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
        final Ast ast = new Ast(input);

        if (ast.hasErrors()) {
            throw new Exception(String.join(" :: ", ast.getErrors()));
        }

        final Num result = ast.eval();

        return result.toString();
    }
}

class Ast {
    private Num x;
    private Num y;
    private Op op;
    private List<String> errors = new ArrayList<String>();

    public Ast(String input) {
        parse(input);
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }

    public List<String> getErrors() {
        return errors;
    }

    private void parse(String input) {
        if (input == null) {
            errors.add("Equation is not provided");
            return;
        }

        final String[] xy = input
                .trim()
                .toUpperCase()
                .split(" ");

        // check equation size
        if (xy.length != 3) {
            errors.add("Equation has incorrect format");
            return;
        }

        // parse first operand
        try {
            x = Num.parse(xy[0]);
        } catch (IllegalArgumentException e) {
            errors.add("First operand is not valid arabic or roman number");
        }

        // parse operation
        try {
            op = Op.parse(xy[1]);
        } catch (IllegalArgumentException e) {
            errors.add("Operation is not supported");
        }

        // parse second operand
        try {
            y = Num.parse(xy[2]);
        } catch (IllegalArgumentException e) {
            errors.add("Second operand is not valid arabic or roman number");
        }

        // check if operands have same type
        if (x.getType() != y.getType())
            errors.add("Can not operate on numbers of different types");
    }

    public Num eval() throws Exception {
        Num result;

        switch (op) {
            case ADD:
                result = x.add(y);
                break;
            case SUB:
                result = x.sub(y);
                break;
            case MUL:
                result = x.mul(y);
                break;
            case DIV:
                result = x.div(y);
                break;
            default:
                throw new Exception("Operation is not defined");
        }

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

    public static Op parse(String input) throws IllegalArgumentException {
        switch (input) {
            case "+":
                return Op.ADD;
            case "-":
                return Op.SUB;
            case "*":
                return Op.MUL;
            case "/":
                return Op.DIV;
            default:
                throw new IllegalArgumentException("Operation is not supported");
        }
    }
}

class Num {
    private int value;
    private NumType type;
    public static String[] basicRoman = {
            "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };
    public static String[] basicArabic = {
            "", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
    };

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

    public static Num parse(String input) throws IllegalArgumentException {
        for (int i = 0; i <= 10; i++) {
            if (basicArabic[i].equals(input)) return new Num(i, NumType.ARABIK);
            else if (basicRoman[i].equals(input)) return new Num(i, NumType.ROMAN);
        }
        throw new IllegalArgumentException();
    }

    public String toString() {
        switch (type) {
            case ARABIK:
                return String.valueOf(value);
            case ROMAN:
                return toRoman();
        }

        return null;
    }

    private String toRoman() {
        StringBuffer buf = new StringBuffer("");

        // whole 100
        int c1 = value / 100;
        if (c1 > 0) buf.append(C(c1));
        // remainder of 100
        int c2 = value % 100;

        // whole 50
        int l1 = c2 / 50;
        buf.append(L(l1));
        // remainder of 50
        int l2 = c2 % 50;

        // whole 10
        int x1 = l2 / 10;
        buf.append(X(x1));
        // remainder of 10
        int x2 = l2 % 10;

        // remainder 1 - 9
        buf.append(oneToNine(x2));

        return buf.toString();
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

    // roman 100
    private static String C(int in) {
        if (in > 0) {
            return "C";
        }

        return "";
    }

    // roman 50
    private static String L(int in) {
        if (in == 4) return "XC";
        if (in > 0) {
            return "L";
        }

        return "";
    }

    // roman 10
    private static String X(int in) {
        if (in == 4) return "XL";
        else if ((in != 0) && (in < 4)) {
            StringBuffer a = new StringBuffer("");
            int i = 0;
            while (i < in) {
                a.append("X");
                i++;
            }
            return a.toString();
        } else return "";
    }

    // roman 1 - 9
    private static String oneToNine(int in) {
        return basicRoman[in];
    }
}

enum NumType {
    ARABIK,
    ROMAN
}
