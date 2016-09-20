package nablarch.common.date;

import java.lang.annotation.Annotation;

/**
 * 入力値を年月日を表す文字列に変換するクラス。
 * <p/>
 * 本クラスで変換するプロパティには、必ず{@link YYYYMMDD}アノテーションを付与しておく必要がある。
 * <p/>
 * <b>バリデーション仕様</b>
 * <p/>
 * {@code @YYYYMMDD(allowFormat=yyyy/MM/dd)}を設定したプロパティに対するバリデーション例を以下に示す。
 * <p/>
 * <pre>
 *     「2011/09/28」:有効。
 *     「20110928」　:有効。年月日の区切り文字(=パターン文字以外の文字)を取り除いたフォーマット(yyyyMMdd)も有効となる。
 *     「2011/02/29」:無効。存在しない日付(閏年以外の年の2/29)。
 *     「2011-09-28」:無効。年月日の区切り文字が異なる。
 *     「2011928」　 :無効。フォーマット(yyyyMMdd)にも一致しない。
 * </pre>
 * <b>国際化</b>
 * <p/>
 * 年月日の記述は、言語によってはフォーマットが異なる(MM/dd/yyyyなど)。</br>
 * Nablarchのカスタムタグで国際化機能を使用した場合、本クラスはカスタムタグで指定されたフォーマットを使用する。
 *
 * @author Kiyohito Itoh
 */

public class YYYYMMDDConvertor extends AbstractDateStringConverter {

    /** コンストラクタ */
    public YYYYMMDDConvertor() {
        super("yyyyMMdd", "yyyymmdd");
    }

    /** {@inheritDoc} */
    @Override
    protected AnnotationData getAnnotationDataFrom(Annotation annotation) {
        if (!(annotation instanceof YYYYMMDD)) {
            return null;
        }
        YYYYMMDD yyyyMm = (YYYYMMDD) annotation;
        AnnotationData data = new AnnotationData();
        data.allowFormat = yyyyMm.allowFormat();
        data.messageId = yyyyMm.messageId();
        return data;
    }

    /** {@inheritDoc} */
    public Class<? extends Annotation> getTargetAnnotation() {
        return YYYYMMDD.class;
    }
}
