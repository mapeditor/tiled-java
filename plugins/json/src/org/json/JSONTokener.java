package org.json;

import java.text.ParseException;

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 * <p>
 * Public Domain 2002 JSON.org
 * @author JSON.org
 * @version 0.1
 */
public class JSONTokener {

    /**
     * The index of the next character.
     */
    private int myIndex;


    /**
     * The source string being tokenized.
     */
    private String mySource;


    /**
     * Construct a JSONTokener from a string.
     *
     * @param s     A source string.
     */
    public JSONTokener(String s) {
        myIndex = 0;
        mySource = s;
    }


    /**
     * Back up one character. This provides a sort of lookahead capability,
     * so that you can test for a digit or letter before attempting to parse
     * the next number or identifier.
     */
    public void back() {
        if (myIndex > 0) {
            myIndex -= 1;
        }
    }



    /**
     * Get the hex value of a character (base16).
     * @param c A character between '0' and '9' or between 'A' and 'F' or
     * between 'a' and 'f'.
     * @return  An int between 0 and 15, or -1 if c was not a hex digit.
     */
    public static int dehexchar(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c + 10 - 'A';
        }
        if (c >= 'a' && c <= 'f') {
            return c + 10 - 'a';
        }
        return -1;
    }


    /**
     * Determine if the source string still contains characters that next()
     * can consume.
     * @return true if not yet at the end of the source.
     */
    public boolean more() {
        return myIndex < mySource.length();
    }


    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     */
    public char next() {
        char c = more() ? mySource.charAt(myIndex) : 0;
        myIndex += 1;
        return c;
    }


    /**
     * Consume the next character, and check that it matches a specified
     * character.
     * @param c The character to match.
     * @return The character.
     * @throws ParseException if the character does not match.
     */
    public char next(char c) throws ParseException {
        char n = next();
        if (n != c) {
            throw syntaxError("Expected '" + c + "' and instead saw '" +
                    n + "'.");
        }
        return n;
    }


    /**
     * Get the next n characters.
     * @param n     The number of characters to take.
     * @return      A string of n characters.
     * @exception ParseException
     *   Substring bounds error if there are not
     *   n characters remaining in the source string.
     */
     public String next(int n) throws ParseException {
         int i = myIndex;
         int j = i + n;
         if (j >= mySource.length()) {
            throw syntaxError("Substring bounds error");
         }
         myIndex += n;
         return mySource.substring(i, j);
     }


    /**
     * Get the next char in the string, skipping whitespace
     * and comments (slashslash and slashstar).
     * @throws ParseException
     * @return  A character, or 0 if there are no more characters.
     */
    public char nextClean() throws java.text.ParseException {
        while (true) {
            char c = next();
            if (c == '/') {
                switch (next()) {
                case '/':
                    do {
                        c = next();
                    } while (c != '\n' && c != '\r' && c != 0);
                    break;
                case '*':
                    while (true) {
                        c = next();
                        if (c == 0) {
                            throw syntaxError("Unclosed comment.");
                        }
                        if (c == '*') {
                            if (next() == '/') {
                                break;
                            }
                            back();
                        }
                    }
                    break;
                default:
                    back();
                    return '/';
                }
            } else if (c == 0 || c > ' ') {
                return c;
            }
        }
    }


    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     * @param quote The quoting character, either <code>"</code>&nbsp;<small>(double quote)</small> or <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return      A String.
     * @exception ParseException Unterminated string.
     */
    public String nextString(char quote) throws ParseException {
        char c;
        StringBuffer sb = new StringBuffer();
        while (true) {
            c = next();
            switch (c) {
            case 0:
            case 0x0A:
            case 0x0D:
                throw syntaxError("Unterminated string");
            case '\\':
                c = next();
                switch (c) {
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'u':
                    sb.append((char)Integer.parseInt(next(4), 16));
                    break;
                case 'x' :
                    sb.append((char) Integer.parseInt(next(2), 16));
                    break;
                default:
                    sb.append(c);
                }
                break;
            default:
                if (c == quote) {
                    return sb.toString();
                }
                sb.append(c);
            }
        }
    }


    /**
     * Get the text up but not including the specified character or the
     * end of line, whichever comes first.
     * @param  d A delimiter character.
     * @return   A string.
     */
    public String nextTo(char d) {
        StringBuffer sb = new StringBuffer();
        while (true) {
            char c = next();
            if (c == d || c == 0 || c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the text up but not including one of the specified delimeter
     * characters or the end of line, which ever comes first.
     * @param delimiters A set of delimiter characters.
     * @return A string, trimmed.
     */
    public String nextTo(String delimiters) {
        char c;
        StringBuffer sb = new StringBuffer();
        while (true) {
            c = next();
            if (delimiters.indexOf(c) >= 0 || c == 0 ||
                    c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, or String, or the JSONObject.NULL object.
     * @exception ParseException The source conform to JSON syntax.
     *
     * @return An object.
     */
    public Object nextValue() throws ParseException {
        char c = nextClean();
        String s;

        if (c == '"' || c == '\'') {
            return nextString(c);
        }
        if (c == '{') {
            back();
            return new JSONObject(this);
        }
        if (c == '[') {
            back();
            return new JSONArray(this);
        }
        StringBuffer sb = new StringBuffer();
        char b = c;
        while (c >= ' ' && c != ':' && c != ',' && c != ']' && c != '}' &&
                c != '/') {
            sb.append(c);
            c = next();
        }
        back();
        s = sb.toString().trim();
        if (s.equals("true")) {
            return Boolean.TRUE;
        }
        if (s.equals("false")) {
            return Boolean.FALSE;
        }
        if (s.equals("null")) {
            return JSONObject.NULL;
        }
        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
            try {
                return new Integer(s);
            } catch (Exception e) {
            }
            try {
                return new Double(s);
            } catch (Exception e) {
            }
        }
        if (s.length() == 0) {
            throw syntaxError("Missing value.");
        }
        return s;
    }


    /**
     * Skip characters until the next character is the requested character.
     * If the requested character is not found, no characters are skipped.
     * @param to A character to skip to.
     * @return The requested character, or zero if the requested character
     * is not found.
     */
    public char skipTo(char to) {
        char c;
        int index = myIndex;
        do {
            c = next();
            if (c == 0) {
                myIndex = index;
                return c;
            }
        } while (c != to);
        back();
        return c;
    }


    /**
     * Skip characters until past the requested string.
     * If it is not found, we are left at the end of the source.
     * @param to A string to skip past.
     */
    public void skipPast(String to) {
        myIndex = mySource.indexOf(to, myIndex);
        if (myIndex < 0) {
            myIndex = mySource.length();
        } else {
            myIndex += to.length();
        }
    }


    /**
     * Make a ParseException to signal a syntax error.
     *
     * @param message The error message.
     * @return  A ParseException object, suitable for throwing
     */
    public ParseException syntaxError(String message) {
        return new ParseException(message + toString(), myIndex);
    }


    /**
     * Make a printable string of this JSONTokener.
     *
     * @return " at character [myIndex] of [mySource]"
     */
    public String toString() {
        return " at character " + myIndex + " of " + mySource;
    }


    /**
     * Unescape the source text. Convert <code>%</code><i>hh</i> sequences to single characters,
     * and convert plus to space. There are Web transport systems that insist on
     * doing unnecessary URL encoding. This provides a way to undo it.
     */
    void unescape() {
        mySource = unescape(mySource);
    }

    /**
     * Convert <code>%</code><i>hh</i> sequences to single characters, and convert plus to space.
     * @param s A string that may contain <code>+</code>&nbsp;<small>(plus)</small> and <code>%</code><i>hh</i> sequences.
     * @return The unescaped string.
     */
    public static String unescape(String s) {
        int len = s.length();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if (c == '+') {
                c = ' ';
            } else if (c == '%' && i + 2 < len) {
                int d = dehexchar(s.charAt(i + 1));
                int e = dehexchar(s.charAt(i + 2));
                if (d >= 0 && e >= 0) {
                    c = (char)(d * 16 + e);
                    i += 2;
                }
            }
            b.append(c);
        }
        return b.toString();
    }
}