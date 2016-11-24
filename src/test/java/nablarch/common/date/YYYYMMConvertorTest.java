package nablarch.common.date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nablarch.core.ThreadContext;
import nablarch.test.support.message.MockStringResourceHolder;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.convertor.Digits;
import nablarch.core.validation.convertor.TestTarget;
import nablarch.core.validation.creator.ReflectionFormCreator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author T.Kawasaki
 */
public class YYYYMMConvertorTest {

    private YYYYMMConvertor convertor = new YYYYMMConvertor();

    /** モックストリングリソースホルダ！ */
    private static MockStringResourceHolder resource = new MockStringResourceHolder();

    private static final String[][] MESSAGES = {
        {"PROP0001", "ja", "プロパティ1", "en", "property1"},
        {"MSG00002", "ja", "{0}がフォーマット通りではありません。", "en", "value if input is not well-formatted."},
        {"MCUSTOM1", "ja", "{0}カスタムエラーメッセージ", "en", "{0}custom error message"}
    };

    @BeforeClass
    public static void setUpClass() {
        SystemRepository.clear();
        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                final Map<String, Object> result = new HashMap<String, Object>();
                result.put("stringResourceHolder", resource);
                return result;
            }
        });
        resource.setMessages(MESSAGES);
        ThreadContext.setLanguage(Locale.JAPANESE);
    }

    @Before
    public void setUp() {
        convertor.setParseFailedMessageId("MSG00002");
    }

    /** {@link YYYYMMDDConvertor#convert}のテスト */
    @Test
    public void testConvert() throws Exception {

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"yyyy/MM|ja"});
        params.put("param_nablarch_formatSpec_Separator", new String[]{"|"});
        ValidationContext<TestTarget> context = createContext(params);
        YYYYMM annotation = getYYYYMM("yyyy/MM");

        // 入力値に対して期待通りの変換がなされること
        doTestConvert(context, annotation, new String[][]{
                // ケース, 入力値, 期待値
                {"フォーマット通り", "2011/09", "201109"},
                {"区切り文字なし", "201109", "201109"},
                {"空文字","", ""},
                {"null", null, null},
        });

        // MM/yyyy形式
        annotation = getYYYYMM("MM/yyyy");
        doTestConvert(context, annotation, new String[][]{
                // ケース, 入力値, 期待値
                {"フォーマット通り", "09/2011", "201109"},
                {"区切り文字なし", "092011", "201109"}
        });
    }

    /**
     * {@link YYYYMMDDConvertor#convert}
     * のテスト(国際化機能を使用する場合) */
    @Test
    public void testConvertForI18N() throws Exception {

        YYYYMM annotation = getYYYYMM("yyyy/MM");
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"yyyymm{yyyy-MM}"});

        ValidationContext<TestTarget> context = createContext(params);

        // 入力値に対して期待通りの変換がなされること
        doTestConvert(context, annotation, new String[][]{
                // ケース, 入力値, 期待値
                {"フォーマット通り", "2011-09", "201109"},
                {"区切り文字なし", "201109", "201109"},
                {"空文字","", ""},
                {"null", null, null},
        });
        
        // ロケールが指定された場合
        params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"yyyymm{MMM yyyy|en}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});

        context = createContext(params);

        doTestConvert(context, annotation, new String[][]{
                // ケース, 入力値, 期待値
                {"ロケール指定", "Nov 2012", "201211"},
                {"ロケール指定かつ区切りなし", "Sep2011", "201109"}
        });
    }

    /**
     * {@link YYYYMMDDConvertor#convert }テスト
     * 関連付けられるアノテーションが異なる場合、例外が発生すること。
     */
    @Test
    public void testConvertFail() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        ValidationContext<TestTarget> context = createContext(params);
        try {
            // 関連付けられるアノテーションが異なる。
            convertor.convert(context, "param", "20110909", digitsAnnotation);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(0, context.getMessages().size());
            assertEquals("Must specify @YYYYMM annotation. property=param", e.getMessage());
        }
    }

    /** {@link YYYYMMDDConvertor#isConvertible}のテスト */
    @Test
    public void testIsConvertible() throws Exception {

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        YYYYMM annotation = getYYYYMM("yyyy/MM");

        String[][] data = {
                // ケース, 入力値
                {"フォーマット通り", "2011/09"},
                {"空文字", ""},
                {"null", null},
                {"区切り文字なし", "201109"}
        };
        // 入力値全て変換可能と判定されること
        doTestIsConvertible(data, params, annotation);
    }

    /** {@link YYYYMMDDConvertor#isConvertible }のテスト(国際化機能を使用する場合) */
    @Test
    public void testIsConvertibleForI18N() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"yyyymm{yyyy-MM|jp}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});
        YYYYMM annotation = getYYYYMM("yyyy/MM");
        String[][] data = {
                {"フォーマット通り", "2011-09"},
                {"空文字", ""},
                {"null", null},
                {"区切り文字なし", "201109"}
        };
        doTestIsConvertible(data, params, annotation);
    }

    private void doTestIsConvertible(String[][] data, Map<String, String[]> params, Annotation annotation) {
        for (String[] e : data) {
            ValidationContext<TestTarget> context = createContext(params);
            assertTrue(e[0], convertor.isConvertible(context, "param", "PRPO001", e[1], annotation));
            assertTrue(e[0], context.isValid());
        }
    }

    /** {@link YYYYMMDDConvertor#isConvertible}のテスト *//** */
    @Test
    public void testIsConvertibleFalse() {

        YYYYMM annotation = getYYYYMM("yyyy/MM");
        String[][] data = {
                // ケース, 入力値, 期待するメッセージID
                {"実在しない日付", "201113", "MSG00002"},
                {"日付にアルファベットがある", "2011/1a", "MSG00002"},
                {"月が3ケタ", "2011/011", "MSG00002"},
                {"区切り文字がハイフン", "2011-09", "MSG00002"} ,
                {"7ケタの数値", "2011111", "MSG00002"},
                {"スペース", " ", "MSG00002"}
        };
        // 入力値が、変換不可と判定され、期待するメッセージが設定されていること
        doTestIsConvertibleFail(data, annotation);
    }

    /**
     * 変換不可能なケース。
     * numberOnlyFormatがnullのケース。
     */
    @Test
    public void testIsConvertibleNumberOnlyFormatNull() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"yyyymm{yyyyMM|jp}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});
        YYYYMM annotation = getYYYYMM("yyyy/MM");
        assertFalse(convertor.isConvertible(createContext(params), "param", "PRPO001", "2011-09", annotation));
    }

    /** {@link YYYYMMDDConvertor#isConvertible}のテスト */
    @Test
    public void testIsConvertibleSpecifiedMessage() {
        YYYYMM annotation = getYYYYMM("yyyy/MM", "MCUSTOM1"); // メッセージIDを明示的に指定
        String[][] data = {
                // ケース, 入力値, 期待するメッセージID
                {"個別メッセージID", "201113", "MCUSTOM1"},
        };
        // 指定したメッセージIDが設定されていること
        doTestIsConvertibleFail(data, annotation);
    }

    private void doTestIsConvertibleFail(String[][] data, Annotation annotation) {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        for (String[] e : data) {
            ValidationContext<TestTarget> context = createContext(params);
            String msg = e[0];
            assertFalse(msg, convertor.isConvertible(context, "param", "PRPO0001", e[1], annotation));
            assertEquals(msg, 1, context.getMessages().size());
            assertEquals(msg, e[2], context.getMessages().get(0).getMessageId());
        }
    }

    /**
     * {@link YYYYMMDDConvertor#isConvertible}のテスト
     * 関連付けられるアノテーションが異なる場合、例外が発生すること。
     */
    @Test
    public void testIsConvertibleFail() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        ValidationContext<TestTarget> context = createContext(params);
        try {
            // 関連付けられるアノテーションが異なる。
            convertor.isConvertible(context, "param", "PRPO0001", "20110909", digitsAnnotation);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(0, context.getMessages().size());
            assertEquals("Must specify @YYYYMM annotation. property=param", e.getMessage());
        }
    }

    /** {@link YYYYMMConvertor#getTargetAnnotation()}のテスト */
    @Test
    public void testGetTargetAnnotation() throws Exception {
        assertThat(convertor.getTargetAnnotation(), is((Object) YYYYMM.class));
    }

    /** {@link YYYYMMConvertor#getTargetClass()}のテスト */
    @Test
    public void testGetTargetClass() {
        assertThat(convertor.getTargetClass(), is((Object) String.class));
    }

    private void doTestConvert(ValidationContext<?> context, Annotation annotation, String[][] testData ) {
        for (String[] data : testData) {
            String actual = (String) convertor.convert(context, "param", data[1], annotation);
            assertThat(data[0], actual, is(data[2]));
        }
    }

    private ValidationContext<TestTarget> createContext(
            Map<String, String[]> params) {
        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                                                     "", TestTarget.class,
                                                     new ReflectionFormCreator(),
                                                     params, "");
        return context;
    }

    private YYYYMM getYYYYMM(final String allowFormat) {
        return getYYYYMM(allowFormat, "");
    }

    private YYYYMM getYYYYMM(final String allowFormat, final String messageId) {
        return new YYYYMM() {
            public Class<? extends Annotation> annotationType() { return YYYYMM.class; }
            public String allowFormat() { return allowFormat; }
            public String messageId() { return messageId; /* default */ }
        };

    }
    private static Digits digitsAnnotation = new Digits() {
        public int integer() { return 0; }
        public int fraction() { return 0; }
        public boolean commaSeparated() { return false; }
        public String messageId() { return null; }
        public Class<? extends Annotation> annotationType() { return null; }
    };
}
