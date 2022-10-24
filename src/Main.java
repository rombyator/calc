import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.print("Enter equation (or q to exit): ");
            final String eq = in.nextLine();
            if (eq.trim().equalsIgnoreCase("q")) break;
            System.out.print("Result: " + calc(eq));
            System.out.println();
            System.out.println();
        }

        in.close();
    }

    public static String calc(String input) throws Exception {
        final Ast ast = Ast.parse(input);
        final Num result = ast.eval();

        return result.toString();
    }
}

class Ast {
    private final Num x;
    private final Num y;
    private final Op op;

    private Ast(Num x, Num y, Op op) {
        this.x = x;
        this.y = y;
        this.op = op;
    }

    public static Ast parse(String input) throws Exception {
        Num x;
        Num y;
        Op op;

        if (input == null)
            throw new Exception("Equation is not provided");

        final String[] xy = input
                .trim()
                .toUpperCase()
                .split(" ");

        // check equation size
        if (xy.length != 3)
            throw new Exception("Equation has incorrect format");

        // parse first operand
        try {
            x = Num.parse(xy[0]);
        } catch (IllegalArgumentException e) {
            throw new Exception("First operand is not valid arabic or roman number");
        }

        // parse operation
        try {
            op = Op.parse(xy[1]);
        } catch (IllegalArgumentException e) {
            throw new Exception("Operation is not supported");
        }

        // parse second operand
        try {
            y = Num.parse(xy[2]);
        } catch (IllegalArgumentException e) {
            throw new Exception("Second operand is not valid arabic or roman number");
        }

        // check if operands have same type
        if (x.getType() != y.getType())
            throw new Exception("Can not operate on numbers of different types");

        return new Ast(x, y, op);
    }

    public Num eval() throws Exception {
        Num result = switch (op) {
            case ADD -> x.add(y);
            case SUB -> x.sub(y);
            case MUL -> x.mul(y);
            case DIV -> x.div(y);
        };

        if (result.getType() == NumType.ROMAN && result.getValue() < 1)
            throw new Exception("Roman numerals must be >= 1");

        return result;
    }
}

enum Op {
    ADD,
    SUB,
    MUL,
    DIV;

    public static Op parse(String input) {
        return switch (input) {
            case "+" -> Op.ADD;
            case "-" -> Op.SUB;
            case "*" -> Op.MUL;
            case "/" -> Op.DIV;
            default -> throw new IllegalArgumentException("Operation is not supported");
        };
    }
}

class Num {
    private final int value;
    private final NumType type;

    private Num(int value, NumType type) {
        this.value = value;
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public NumType getType() {
        return type;
    }

    public static Num parse(String input) {
        try {
            final int value = Integer.parseInt(input);
            if (isValidValue(value))
                return new Num(value, NumType.ARABIC);
        } catch (NumberFormatException e) {
            if (isValidRoman(input)) {
                final int value = romanToInt(input);
                if (isValidValue(value))
                    return new Num(value, NumType.ROMAN);
            }
        }

        throw new IllegalArgumentException("Argument is not valid Roman or Arabic number");
    }

    private static boolean isValidValue(int value) {
        return value <= 10;
    }

    private static boolean isValidRoman(String romanStr) {
        return romanStr.matches("(X|IX|IV|V?I{0,3})");
    }

    public String toString() {
        return switch (type) {
            case ARABIC -> String.valueOf(value);
            case ROMAN -> toRoman();
        };

    }

    private static int romanToInt(String rs) {
        int result = 0;

        Map<Character, Integer> values = new HashMap<>();
        values.put('I', 1);
        values.put('V', 5);
        values.put('X', 10);
        values.put('L', 50);
        values.put('C', 100);

        for (int i = 0; i < rs.length() - 1; i++) {
            int n = values.get(rs.charAt(i));
            int m = values.get(rs.charAt(i + 1));
            if (i + 1 == rs.length() || n >= m) {
                result += n;
            } else {
                result -= n;
            }
        }

        int lastCharValue = values.get(rs.charAt(rs.length() - 1));
        result += lastCharValue;

        return result;
    }

    private String toRoman() {
        StringBuilder result = new StringBuilder();
        int number = this.value;
        int[] numbers = {100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] romans = {"C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        for (int i = 0; i < numbers.length; i++) {
            while (number >= numbers[i]) {
                number -= numbers[i];
                result.append(romans[i]);
            }
        }

        return result.toString();
    }

    public Num add(Num other) {
        final int value = this.value + other.getValue();

        return new Num(value, this.type);
    }

    public Num sub(Num other) {
        final int value = this.value - other.getValue();

        return new Num(value, this.type);
    }

    public Num mul(Num other) {
        final int value = this.value * other.getValue();

        return new Num(value, this.type);
    }

    public Num div(Num other) {
        final int value = this.value / other.getValue();

        return new Num(value, this.type);
    }
}

enum NumType {
    ARABIC,
    ROMAN
}
