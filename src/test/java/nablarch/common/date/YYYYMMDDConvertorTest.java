package nablarch.common.date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nablarch.core.ThreadContext;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.convertor.Digits;
import nablarch.core.validation.convertor.TestTarget;
import nablarch.core.validation.creator.ReflectionFormCreator;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link YYYYMMDDConvertor}のテスト
 * @author Tomokazu Kagawa
 */
public class YYYYMMDDConvertorTest {

    private static YYYYMMDDConvertor convertor;

    /** モックストリングリソースホルダ */
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
        convertor = new YYYYMMDDConvertor();
        convertor.setParseFailedMessageId("MSG00002");
        ThreadContext.setLanguage(Locale.JAPANESE);
    }

    /** {@link YYYYMMDDConvertor#convert }テスト */
    @Test
    public void testConvert() throws Exception {

        convertor.setParseFailedMessageId("MSG00002");

        YYYYMMDD annotation = new YYYYMMDD() {
            public Class<? extends Annotation> annotationType() { return YYYYMMDD.class; }
            public String allowFormat() { return "yyyy/MM/dd"; }
            public String messageId() { return ""; /* default */ }
        };

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = createContext(params);

        // フォーマット通り
        assertEquals("20110909", convertor.convert(context, "param", "2011/09/09", annotation));

        // 区切り文字なし
        assertEquals("20110909", convertor.convert(context, "param", "20110909", annotation));

        // 閏年
        assertEquals("20000229", convertor.convert(context, "param", "2000/02/29", annotation));
        assertEquals("20000228", convertor.convert(context, "param", "2000/02/28", annotation));
        assertEquals("20000229", convertor.convert(context, "param", "20000229", annotation));
        assertEquals("20000228", convertor.convert(context, "param", "20000228", annotation));

        // 空文字
        assertEquals("", convertor.convert(context, "param", "", annotation));

        // null
        assertNull(convertor.convert(context, "param", null, annotation));

        // MM/dd/yyyy
        annotation = new YYYYMMDD() {
            public Class<? extends Annotation> annotationType() { return YYYYMMDD.class; }
            public String allowFormat() { return "MM/dd/yyyy"; }
            public String messageId() { return ""; /* default */ }
        };

        // フォーマット通り
        assertEquals("20110928", convertor.convert(context, "param", "09/28/2011", annotation));

        // 区切り文字なし
        assertEquals("20110928", convertor.convert(context, "param", "09282011", annotation));

    }

    /** {@link YYYYMMDDConvertor#convert }テスト(国際化機能を使用する場合) */
    @Test
    public void testConvertForI18N() throws Exception {

        convertor.setParseFailedMessageId("MSG00002");

        YYYYMMDD annotation = new YYYYMMDD() {
            public Class<? extends Annotation> annotationType() { return YYYYMMDD.class; }
            public String allowFormat() { return "yyyy/MM/dd"; }
            public String messageId() { return ""; /* default */ }
        };

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"yyyymmdd{yyyy-MM-dd}"});

        ValidationContext<TestTarget> context = createContext(params);

        // フォーマット通り
        assertEquals("20110909", convertor.convert(context, "param", "2011-09-09", annotation));

        // 区切り文字なし
        assertEquals("20110909", convertor.convert(context, "param", "20110909", annotation));

        // 閏年
        assertEquals("20000229", convertor.convert(context, "param", "2000-02-29", annotation));
        assertEquals("20000228", convertor.convert(context, "param", "2000-02-28", annotation));
        assertEquals("20000229", convertor.convert(context, "param", "20000229", annotation));
        assertEquals("20000228", convertor.convert(context, "param", "20000228", annotation));

        // 空文字
        assertEquals("", convertor.convert(context, "param", "", annotation));

        // null
        assertNull(convertor.convert(context, "param", null, annotation));

        // ロケールが指定された場合
        params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"yyyymmdd{dd MMM yyyy|en}"});
        params.put("param_nablarch_formatSpec_separator", new String[]{"|"});

        context = createContext(params);

        assertEquals("20121113", convertor.convert(context, "param", "13 Nov 2012", annotation));
    }

    /** {@link YYYYMMDDConvertor#isConvertible }テスト */
    @Test
    public void testIsConvertible() throws Exception {

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});

        ValidationContext<TestTarget> context = null;

        YYYYMMDD annotation = new YYYYMMDD() {
            public Class<? extends Annotation> annotationType() { return YYYYMMDD.class; }
            public String allowFormat() { return "yyyy/MM/dd"; }
            public String messageId() { return ""; /* default */ }
        };

        // ----------- 正常系ここから ----------- 
        {
            context = createContext(params);

            // フォーマット通り
            assertTrue(convertor.isConvertible(context, "param", "PROP0001", "2011/09/09", annotation));
            assertEquals(0, context.getMessages().size());
        }

        {
            context = createContext(params);

            // 空文字
            assertTrue(convertor.isConvertible(context, "param", "PROP0001", "", annotation));
            assertEquals(0, context.getMessages().size());
        }

        {
            context = createContext(params);

            // null
            assertTrue(convertor.isConvertible(context, "param", "PROP0001", null, annotation));
            assertEquals(0, context.getMessages().size());
        }

        {
            context = createContext(params);

            // 区切り文字なし
            assertTrue(convertor.isConvertible(context, "param", "PRPO0001", "20110909", annotation));
            assertEquals(0, context.getMessages().size());
        }
        // ----------- 正常系ここまで ----------- 

        // ----------- 異常系ここから ----------- 
        {
            context = createContext(params);

            // 実在しない日付
            assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011/02/29", annotation));
            assertEquals(1, context.getMessages().size());
            assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(0).formatMessage());
        }

        {
            context = createContext(params);

            // 不正な日付文字列(日付にアルファベットがある)
            assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011/09/09a", annotation));
            assertEquals(1, context.getMessages().size());
            assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(0).formatMessage());
        }

        {
            context = createContext(params);

            // 不正な日付文字列(日付が3ケタ)
            assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011/09/012", annotation));
            assertEquals(1, context.getMessages().size());
            assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(0).formatMessage());
        }

        {
            context = createContext(params);

            // 不正な日付文字列(区切り文字がハイフン)
            assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011-09-09", annotation));
            assertEquals(1, context.getMessages().size());
            assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(0).formatMessage());
        }

        {
            context = createContext(params);

            // 不正な日付文字列(7ケタの数値)
            assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011909", annotation));
            assertEquals(1, context.getMessages().size());
            assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(0).formatMessage());
        }

        {
            context = createContext(params);

            // 空白
            assertFalse(convertor.isConvertible(context, "param", "PRPO0001", " ", annotation));
            assertEquals(1, context.getMessages().size());
            assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(0).formatMessage());
        }

        {
            context = createContext(params);

            // 空白
            assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "  ", annotation));
            assertEquals(1, context.getMessages().size());
            assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(0).formatMessage());
        }

        try {
            // 関連付けられるアノテーションが異なる。
            Digits digitsAnnotation = new Digits() {
                public int integer() { return 0; }
                public int fraction() { return 0; }
                public boolean commaSeparated() { return false; }
                public String messageId() { return null; }
                public Class<? extends Annotation> annotationType() { return null; }
            };

            context = createContext(params);
            convertor.isConvertible(context, "param", "PRPO0001", "20110909", digitsAnnotation);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(0, context.getMessages().size());
            assertEquals("Must specify @YYYYMMDD annotation. property=param", e.getMessage());
        }

        {
            // 個別メッセージID
            annotation = new YYYYMMDD() {
                public Class<? extends Annotation> annotationType() { return YYYYMMDD.class; }
                public String allowFormat() { return "yyyy/MM/dd"; }
                public String messageId() { return "MCUSTOM1"; }
            };

            context = createContext(params);

            assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011/09/09a", annotation));
            assertEquals(1, context.getMessages().size());
            assertEquals("PRPO0001カスタムエラーメッセージ", context.getMessages().get(0).formatMessage());
        }
        // ----------- 異常系ここまで ----------- 

    }

    private ValidationContext<TestTarget> createContext(
            Map<String, String[]> params) {
        ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                                                     "", TestTarget.class,
                                                     new ReflectionFormCreator(),
                                                     params, "");
        return context;
    }

    @Test
    public void testIsConvertibleForI18N() throws Exception {

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("param", new String[]{"10"});
        params.put("param_nablarch_formatSpec", new String[]{"yyyymmdd{yyyy-MM-dd}"});

        ValidationContext<TestTarget> context = createContext(params);

        YYYYMMDD annotation = new YYYYMMDD() {
            public Class<? extends Annotation> annotationType() { return YYYYMMDD.class; }
            public String allowFormat() { return "yyyy/MM/dd"; }
            public String messageId() { return ""; /* default */ }
        };

        // フォーマット通り
        assertTrue(convertor.isConvertible(context, "param", "PROP0001", "2011-09-09", annotation));

        // 区切り文字なし
        assertTrue(convertor.isConvertible(context, "param", "PRPO0001", "20110909", annotation));

        // 不正な日付文字列
        assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011-09-09a", annotation));
        assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(0).formatMessage());
        assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011-09-012", annotation));
        assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(1).formatMessage());
        assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011/09/09", annotation));
        assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(2).formatMessage());
        assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011909", annotation));
        assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(3).formatMessage());

        // 空文字
        assertTrue(convertor.isConvertible(context, "param", "PROP0001", "", annotation));

        // null
        assertTrue(convertor.isConvertible(context, "param", null, "", annotation));

        // 空白
        assertFalse(convertor.isConvertible(context, "param", "PRPO0001", " ", annotation));
        assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(4).formatMessage());
        assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "  ", annotation));
        assertEquals("PRPO0001がフォーマット通りではありません。", context.getMessages().get(5).formatMessage());

        // 関連付けられるアノテーションが異なる。
        Digits digitsAnnotation = new Digits() {
            public int integer() { return 0; }
            public int fraction() { return 0; }
            public boolean commaSeparated() { return false; }
            public String messageId() { return null; }
            public Class<? extends Annotation> annotationType() { return null; }
        };
        try {
            convertor.isConvertible(context, "param", "PRPO0001", "20110909", digitsAnnotation);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Must specify @YYYYMMDD annotation. property=param", e.getMessage());
        }

        // 個別メッセージID
        annotation = new YYYYMMDD() {
            public Class<? extends Annotation> annotationType() { return YYYYMMDD.class; }
            public String allowFormat() { return "yyyy/MM/dd"; }
            public String messageId() { return "MCUSTOM1"; }
        };

        assertFalse(convertor.isConvertible(context, "param", "PRPO0001", "2011-09-09a", annotation));
        assertEquals("PRPO0001カスタムエラーメッセージ", context.getMessages().get(6).formatMessage());
    }

    /** {@link YYYYMMDDConvertor#getTargetAnnotation()}のテスト */
    @Test
    public void testGetTargetAnnotation() throws Exception {
        assertTrue(new YYYYMMDDConvertor().getTargetAnnotation() == YYYYMMDD.class);
    }

    /** {@link YYYYMMDDConvertor#getTargetClass()}のテスト */
    @Test
    public void testGetTargetClass() {
        junit.framework.Assert.assertTrue(new YYYYMMDDConvertor().getTargetClass() == String.class);
    }
}
