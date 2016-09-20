package nablarch.common.date;


import nablarch.core.util.DateUtil;
import nablarch.core.util.FormatSpec;
import nablarch.core.util.I18NUtil;
import nablarch.core.util.StringUtil;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;
import nablarch.core.validation.convertor.ConversionUtil;
import nablarch.core.validation.convertor.ExtendedStringConvertor;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日付を表す文字列を値に変換する抽象クラス。
 *
 * @author T.Kawasaki
 * @see YYYYMMDDConvertor
 * @see YYYYMMConvertor
 */
public abstract class AbstractDateStringConverter implements ExtendedStringConvertor {

    /** フォーマットに使用する日付パターン */
    private final String datePattern;

    /** "データタイプ{パターン}"形式のデータタイプ */
    private final String dataType;

    /**
     * コンストラクタ。
     *
     * @param datePattern フォーマットに使用する日付パターン
     * @param dataType    "データタイプ{パターン}"形式のデータタイプ
     */
    protected AbstractDateStringConverter(String datePattern, String dataType) {
        this.datePattern = datePattern;
        this.dataType = dataType;
    }

    /** 指定された日付への変換失敗時のメッセージID */
    private String parseFailedMessageId;

    /**
     * 指定された日付文字列への変換失敗時のメッセージIDを設定する。
     *
     * @param parseFailedMessageId 指定された日付文字列への変換失敗時のメッセージID
     */
    public void setParseFailedMessageId(String parseFailedMessageId) {
        this.parseFailedMessageId = parseFailedMessageId;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * 値がnullまたは空文字の場合は値をそのまま返す。
     * 値の変換は、{@link #convert(String, String)}メソッドに委譲する。
     */
    public <T> Object convert(ValidationContext<T> context, String propertyName, Object value, Annotation annotation) {

        String stringValue = (String) value;
        if (StringUtil.isNullOrEmpty(stringValue)) {
            // nullまたは空文字の場合は値をそのまま返す。
            return stringValue;
        }

        AnnotationData data = getAnnotationDataFrom(annotation);
        if (data == null) {
            throw new IllegalArgumentException(
                    "Must specify @" + getTargetAnnotation().getSimpleName() + " annotation. property=" + propertyName);
        }
        FormatSpec formatSpec = getFormatSpec(context, propertyName, data.allowFormat);
        return convert(stringValue, formatSpec);
    }

    /**
     * 指定されたフォーマット仕様で値を変換する。<br/>
     * <p>
     * はじめに指定されたフォーマット仕様で値の解析を試みる。
     * 指定されたフォーマット仕様で解析できない場合は、
     * {@link #getNumbersOnlyFormat(String)}メソッドを使用し、
     * 日付文字列の区切り文字を取り除いたフォーマットで解析する。
     * </p>
     * <p>
     * 最後に解析結果として取得できるDateオブジェクトを、所定のパターン({@link #datePattern})の
     * 文字列に変換する。
     * </p>
     *
     * @param value  値
     * @param formatSpec フォーマット仕様
     * @return 変換後の値
     */
    protected String convert(String value, FormatSpec formatSpec) {

        String format = formatSpec.getFormatOfPattern();
        String language = formatSpec.getAdditionalInfoOfPattern();
        Locale locale = StringUtil.hasValue(language) ? I18NUtil.createLocale(language) : null;

        Date date = locale == null
                        ? DateUtil.getParsedDate(value, format)
                        : DateUtil.getParsedDate(value, format, locale);
        if (date == null) {
            date = locale == null
                        ? DateUtil.getParsedDate(value, getNumbersOnlyFormat(format))
                        : DateUtil.getParsedDate(value, getNumbersOnlyFormat(format), locale);
        }
        return new SimpleDateFormat(datePattern).format(date);
    }

    /** {@inheritDoc} */
    public Class<?> getTargetClass() {
        return String.class;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * 値がnullまたは空文字の場合はconvertメソッドでnullを返すため、本メソッドはtrueを返す。
     * 値の変換可否チェックは、{@link #isConvertible(String, String)}メソッドに委譲する。
     * <p/>
     * 値が変換不可の場合は、バリデーション結果メッセージを設定しfalseを返す。
     * メッセージIDは、{@link YYYYMMDD}アノテーションのmessageId属性の値を使用する。
     * {@link YYYYMMDD}アノテーションにメッセージIDが指定されていない場合は、
     * 本クラスのparseFailedMessageIdプロパティの値をメッセージIDに使用する。
     */
    public <T> boolean isConvertible(ValidationContext<T> context, String propertyName,
                                     Object propertyDisplayName, Object value, Annotation format) {

        String stringValue = (String) value;
        if (StringUtil.isNullOrEmpty(stringValue)) {
            // nullまたは空文字の場合はconvertメソッドでnullを返すためtrueを返す。
            return true;
        }

        AnnotationData data = getAnnotationDataFrom(format);
        if (data == null) {
            throw new IllegalArgumentException(
                    "Must specify @" + getTargetAnnotation().getSimpleName() + " annotation. property=" + propertyName);
        }
        if (!isConvertible(stringValue, getFormatSpec(context, propertyName, data.allowFormat))) {
            // 指定されたフォーマットで変換不可
            // かつ日付文字列の区切り文字を取り除いたフォーマットで変換不可な場合
            String messageId = data.messageId;
            ValidationResultMessageUtil.addResultMessage(
                    context, propertyName,
                    StringUtil.hasValue(messageId) ? messageId : parseFailedMessageId,
                    propertyDisplayName);
            return false;
        }

        return true;
    }

    /**
     * 指定されたフォーマット仕様で値が変換可能か否かを判定する。
     * <pre>
     * はじめに指定されたフォーマット仕様で値が変換可能か否かを判定する。
     * 指定されたフォーマット仕様で変換不可の場合は、
     * {@link #getNumbersOnlyFormat(String)}メソッドを使用し、
     * 日付文字列の区切り文字を取り除いたフォーマットで変換可能か否かを判定する。
     * </pre>
     *
     * @param value  値
     * @param formatSpec フォーマット仕様
     * @return 変換可能な場合はtrue
     */
    protected boolean isConvertible(String value, FormatSpec formatSpec) {

        String format = formatSpec.getFormatOfPattern();
        String language = formatSpec.getAdditionalInfoOfPattern();
        Locale locale = StringUtil.hasValue(language) ? I18NUtil.createLocale(language) : null;

        if (locale == null
                ? DateUtil.isValid(value, format)
                : DateUtil.isValid(value, format, locale)) {
            return true;
        }
        String numbersOnlyFormat = getNumbersOnlyFormat(format);
        return numbersOnlyFormat != null
                && (locale == null
                        ? DateUtil.isValid(value, numbersOnlyFormat)
                        : DateUtil.isValid(value, numbersOnlyFormat, locale));
    }

    /**
     * プロパティの変換に使用するフォーマット仕様を取得する。
     * <p/>
     * {@link nablarch.core.validation.convertor.ConversionUtil#getFormatSpec(ValidationContext, String)}を呼び出し、
     * プロパティに対する有効なフォーマット仕様(yyyymmddなど)を取得して返す。
     * <p/>
     * プロパティに対する有効なフォーマット仕様が存在しない場合は、
     * allowFormatから生成したフォーマット仕様を返す。
     *
     * @param <T>          バリデーション結果で取得できる型
     * @param context      ValidationContext
     * @param propertyName プロパティ名
     * @param allowFormat  フォーマット
     * @return 変換に使用するフォーマット仕様
     */
    protected <T> FormatSpec getFormatSpec(ValidationContext<T> context, String propertyName, String allowFormat) {
        FormatSpec formatSpec = ConversionUtil.getFormatSpec(context, propertyName);
        return formatSpec == null || !dataType.equals(formatSpec.getDataType())
                ? FormatSpec.valueOf(String.format("%s{%s}", dataType, allowFormat), "|")
                : formatSpec;
    }

    /**
     * フォーマット文字列から日付文字列の区切り文字を取り除いた値を返す。
     * <p/>
     * {@link DateUtil#getNumbersOnlyFormat(String)}に処理を委譲する。
     *
     * @param format フォーマット文字列
     * @return フォーマット文字列から年月日の区切り文字を取り除いた値
     */
    protected String getNumbersOnlyFormat(String format) {
        return DateUtil.getNumbersOnlyFormat(format);
    }


    /**
     * アノテーションから値を取得する。
     *
     * @param annotation アノテーション
     * @return アノテーションに設定された値
     */
    protected abstract AnnotationData getAnnotationDataFrom(Annotation annotation);

    /** アノテーションから取得したデータを保持するクラス。*/
    static class AnnotationData {
        /** 入力値として許容する年月フォーマット。 */
        String allowFormat;                 // SUPPRESS CHECKSTYLE 内部のみで使用し、カプセル化が不要なため
        /** 変換失敗時のメッセージID。 */
        String messageId;                   // SUPPRESS CHECKSTYLE 内部のみで使用し、カプセル化が不要なため
    }

}
