package nablarch.common.date;

import java.lang.annotation.Annotation;

/**
 * 入力値を年月を表す文字列に変換するクラス。
 * <p/>
 * 本クラスで変換するプロパティには、必ず{@link YYYYMM}アノテーションを付与しておく必要がある。
 * <p/>
 * <b>バリデーション仕様</b>
 * <p/>
 * {@code @YYYYMM(allowFormat=yyyy/MM)}を設定したプロパティに対するバリデーション例を以下に示す。
 * <p/>
 * <pre>
 *     「2011/09」:有効
 *     「201109」 :有効。年月の区切り文字(=パターン文字以外の文字)を取り除いたフォーマット(yyyyMM)も有効となる。
 *     「2011/13」:無効。存在しない日付(閏年以外の年の2/29)。
 *     「2011-09」:無効。年月の区切り文字が異なる。
 *     「20113」  :無効。フォーマット(yyyyMM)にも一致しない。
 * </pre>
 * <b>国際化</b>
 * <p/>
 * 年月の記述は、言語によってはフォーマットが異なる(MM/yyyyなど)。</br>
 * Nablarchのカスタムタグで国際化機能を使用した場合、本クラスはカスタムタグで指定されたフォーマットを使用する。
 *
 * @author T.Kawasaki
 */
public class YYYYMMConvertor extends AbstractDateStringConverter {

    /** コンストラクタ。 */
    public YYYYMMConvertor() {
        super("yyyyMM", "yyyymm");
    }

    /** {@inheritDoc} */
    @Override
    protected AnnotationData getAnnotationDataFrom(Annotation annotation) {
        if (!(annotation instanceof YYYYMM)) {
            return null;
        }
        YYYYMM yyyyMm = (YYYYMM) annotation;
        AnnotationData data = new AnnotationData();
        data.allowFormat = yyyyMm.allowFormat();
        data.messageId = yyyyMm.messageId();
        return data;
    }

    /** {@inheritDoc} */
    public Class<? extends Annotation> getTargetAnnotation() {
        return YYYYMM.class;
    }
}
