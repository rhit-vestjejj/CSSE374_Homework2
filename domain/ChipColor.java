package domain;

public enum ChipColor {
    RED, BLUE, GREEN, BLACK, WHITE;

    public static ChipColor fromChar(char c) {
        return switch (Character.toUpperCase(c)) {
            case 'R' -> RED;
            case 'B' -> BLUE;
            case 'G' -> GREEN;
            case 'K' -> BLACK; // K used for blacK
            case 'W' -> WHITE;
            default -> throw new IllegalArgumentException("Bad color char: " + c);
        };
    }

    public char toChar() {
        return switch (this) {
            case RED -> 'R';
            case BLUE -> 'B';
            case GREEN -> 'G';
            case BLACK -> 'K';
            case WHITE -> 'W';
        };
    }
}