package info.kgeorgiy.ja.petrasiuk.lambda;

public class CommonSuffixHolder implements Holder<CharSequence, String, CommonSuffixHolder> {
    private CharSequence field;

    @Override
    public void set(CharSequence other) {
        if (field == null) {
            field = other;
            return;
        }
        int i = 0;
        StringBuilder prefix = new StringBuilder();
        while (i < other.length() && i < field.length() && field.charAt(field.length() - i - 1) == other.charAt(other.length() - i - 1)) {
            prefix.append(field.charAt(field.length() - i - 1));
            i++;
        }
        field = prefix.reverse();
    }

    @Override
    public String get() {
        if (field == null) {
            return "";
        }
        return field.toString();
    }

    @Override
    public CommonSuffixHolder merge(CommonSuffixHolder other) {
        set(other.field);
        return this;
    }
}
