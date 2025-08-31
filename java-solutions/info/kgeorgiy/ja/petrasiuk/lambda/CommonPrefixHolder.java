package info.kgeorgiy.ja.petrasiuk.lambda;

public class CommonPrefixHolder implements Holder<CharSequence, String, CommonPrefixHolder> {
    private CharSequence field;

    @Override
    public void set(CharSequence other) {
        if (field == null) {
            field = other;
            return;
        }
        int i = 0;
        StringBuilder prefix = new StringBuilder();
        while (i < other.length() && i < field.length() && field.charAt(i) == other.charAt(i)) {
            prefix.append(field.charAt(i));
            i++;
        }
        field = prefix;
    }

    @Override
    public String get() {
        if (field == null) {
            return "";
        }
        return field.toString();
    }

    @Override
    public CommonPrefixHolder merge(CommonPrefixHolder other) {
        set(other.field);
        return this;
    }
}
