// By Ethan Spiece

package presentation;

import domain.*;
import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SplendorFrame extends JFrame {
    private final Game game;

    private final JLabel currentPlayerLabel = new JLabel();
    private final JLabel p1Label = new JLabel();
    private final JLabel p2Label = new JLabel();
    private final JLabel errorLabel = new JLabel();
    private final JTextArea leaderboardArea = new JTextArea(6, 18);

    private final JPanel cardsPanel = new JPanel(new GridLayout(3, 5, 8, 8));
    private static final DateTimeFormatter LEADERBOARD_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SplendorFrame(Game game) {
        super("Mini-Splendor");
        this.game = game;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        redraw();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton newGame = new JButton("New Game");
        newGame.addActionListener(e -> onNewGame());

        top.add(currentPlayerLabel);
        top.add(newGame);
        return top;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new BorderLayout(10, 10));

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(BorderFactory.createTitledBorder("Cards to Buy"));
        left.add(cardsPanel, BorderLayout.CENTER);

        JPanel right = new JPanel(new GridLayout(3, 1, 8, 8));
        right.setBorder(BorderFactory.createTitledBorder("Players & Leaderboard"));
        right.add(wrapLabel(p1Label, "Player 1"));
        right.add(wrapLabel(p2Label, "Player 2"));
        right.add(buildLeaderboardPanel());

        center.add(left, BorderLayout.CENTER);
        center.add(right, BorderLayout.EAST);
        return center;
    }

    private JPanel wrapLabel(JLabel label, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildLeaderboardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Leaderboard (Last 4)"));

        leaderboardArea.setEditable(false);
        leaderboardArea.setLineWrap(true);
        leaderboardArea.setWrapStyleWord(true);
        leaderboardArea.setPreferredSize(new Dimension(220, 120));

        p.add(new JScrollPane(leaderboardArea), BorderLayout.CENTER);
        return p;
    }

    private JComponent buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout(8, 8));

        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        chipRow.setBorder(BorderFactory.createTitledBorder("Take Chips"));

        for (ChipColor c : ChipColor.values()) {
            JButton b = new JButton(c.name());
            b.addActionListener(e -> onChipClicked(c));
            chipRow.add(b);
        }

        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        bottom.add(chipRow, BorderLayout.CENTER);
        bottom.add(errorLabel, BorderLayout.SOUTH);
        return bottom;
    }

    private void onChipClicked(ChipColor color) {
        try {
            game.takeChip(color);
        } catch (IllegalMoveException ex) {
            // Domain decides legality; UI just shows the message.  [oai_citation:8‡374 Homework 2 - minisplendor-Steve (2).pdf](sediment://file_000000007540722f9e8bbcb3aca3178b)
        }
        redraw();
    }

    private void onCardClicked(String cardId) {
        try {
            game.buyCard(cardId);
        } catch (IllegalMoveException ex) {
        }
        redraw();
    }

    private void onNewGame() {
        game.startNewGame();
        redraw();
    }

    public void redraw() {
        currentPlayerLabel.setText("Current Player: " + game.getCurrentPlayerNumber());

        p1Label.setText(playerText(0));
        p2Label.setText(playerText(1));

        // Cards
        cardsPanel.removeAll();
        List<Card> cards = game.getBoard().getAvailable();
        for (Card c : cards) {
            JButton cardButton = new JButton("<html><center>VP: " + c.getVictoryPoints()
                    + "<br/>" + c.costString() + "</center></html>");
            cardButton.addActionListener(e -> onCardClicked(c.getId()));
            cardsPanel.add(cardButton);
        }

        // Fill remaining slots to keep grid stable
        for (int i = cards.size(); i < 15; i++) {
            cardsPanel.add(new JLabel(""));
        }

        errorLabel.setText(game.getLastError());
        leaderboardArea.setText(leaderboardText());

        revalidate();
        repaint();
    }

    private String playerText(int idx) {
        Player p = game.getPlayer(idx);
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("Chips: ");
        for (ChipColor c : ChipColor.values()) {
            sb.append(c.toChar()).append(p.getChips(c)).append(" ");
        }
        sb.append("<br/>Victory Points: ").append(p.getVictoryPoints());
        sb.append("</html>");
        return sb.toString();
    }

    private String leaderboardText() {
        List<Leaderboard.Entry> entries = game.getLeaderboardEntries();
        if (entries.isEmpty()) return "No completed games yet.";

        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for (Leaderboard.Entry e : entries) {
            String when = LEADERBOARD_TIME.format(
                    Instant.ofEpochMilli(e.getTimestampMillis())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
            );
            String winner = winnerLabel(e.getPlayer1Vp(), e.getPlayer2Vp());
            sb.append(rank++)
              .append(". ")
              .append(when)
              .append("  P1 ")
              .append(e.getPlayer1Vp())
              .append(" - P2 ")
              .append(e.getPlayer2Vp())
              .append("  (")
              .append(winner)
              .append(")");
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private String winnerLabel(int p1Vp, int p2Vp) {
        if (p1Vp > p2Vp) return "P1";
        if (p2Vp > p1Vp) return "P2";
        return "Tie";
    }
}
