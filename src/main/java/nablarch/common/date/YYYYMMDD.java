package nablarch.common.date;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ConversionFormat;

/**
 * {@link YYYYMMDDConvertor}で許容する年月日フォーマットを指定するアノテーション。
 * 
 * @author Kiyohito Itoh
 */
@ConversionFormat
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface YYYYMMDD {

    /**
     * 入力値として許容する年月日フォーマット。
     * <p/>
     * {@link java.text.SimpleDateFormat}が規定している構文で指定すること。
     * パターン文字は、y(年)、M(月)、d(月における日)のみ指定可能。
     */
    String allowFormat() default "";

    /**
     * 変換失敗時のメッセージID。
     * <p/>
     * 指定がない場合は{@link YYYYMMDDConvertor}に設定されたメッセージIDを使用する。
     */
    String messageId() default "";
}
