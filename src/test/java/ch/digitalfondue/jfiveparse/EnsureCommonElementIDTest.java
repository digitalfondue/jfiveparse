package ch.digitalfondue.jfiveparse;

import static ch.digitalfondue.jfiveparse.Common.*;

import org.junit.Assert;
import org.junit.Test;

public class EnsureCommonElementIDTest {

    @Test
    public void ensureCommonElementIDRanges() {
        var addressToXmpSpecial = new byte[]{
                ELEMENT_ADDRESS_ID,
                ELEMENT_APPLET_ID,
                ELEMENT_AREA_ID,
                ELEMENT_ARTICLE_ID,
                ELEMENT_ASIDE_ID,
                ELEMENT_BASE_ID,
                ELEMENT_BASEFONT_ID,
                ELEMENT_BGSOUND_ID,
                ELEMENT_BLOCKQUOTE_ID,
                ELEMENT_BODY_ID,
                ELEMENT_BR_ID,
                ELEMENT_BUTTON_ID,
                ELEMENT_CAPTION_ID,
                ELEMENT_CENTER_ID,
                ELEMENT_COL_ID,
                ELEMENT_COLGROUP_ID,
                ELEMENT_DD_ID,
                ELEMENT_DETAILS_ID,
                ELEMENT_DIR_ID,
                ELEMENT_DIV_ID,
                ELEMENT_DL_ID,
                ELEMENT_DT_ID,
                ELEMENT_EMBED_ID,
                ELEMENT_FIELDSET_ID,
                ELEMENT_FIGCAPTION_ID,
                ELEMENT_FIGURE_ID,
                ELEMENT_FOOTER_ID,
                ELEMENT_FORM_ID,
                ELEMENT_FRAME_ID,
                ELEMENT_FRAMESET_ID,
                ELEMENT_H1_ID,
                ELEMENT_H2_ID,
                ELEMENT_H3_ID,
                ELEMENT_H4_ID,
                ELEMENT_H5_ID,
                ELEMENT_H6_ID,
                ELEMENT_HEAD_ID,
                ELEMENT_HEADER_ID,
                ELEMENT_HGROUP_ID,
                ELEMENT_HR_ID,
                ELEMENT_HTML_ID,
                ELEMENT_IFRAME_ID,
                ELEMENT_IMG_ID,
                ELEMENT_INPUT_ID,
                ELEMENT_LI_ID,
                ELEMENT_LINK_ID,
                ELEMENT_LISTING_ID,
                ELEMENT_MAIN_ID,
                ELEMENT_MARQUEE_ID,
                ELEMENT_MENU_ID,
                ELEMENT_META_ID,
                ELEMENT_NAV_ID,
                ELEMENT_NOEMBED_ID,
                ELEMENT_NOFRAMES_ID,
                ELEMENT_NOSCRIPT_ID,
                ELEMENT_OBJECT_ID,
                ELEMENT_OL_ID,
                ELEMENT_P_ID,
                ELEMENT_PARAM_ID,
                ELEMENT_PLAINTEXT_ID,
                ELEMENT_PRE_ID,
                ELEMENT_SCRIPT_ID,
                ELEMENT_SECTION_ID,
                ELEMENT_SELECT_ID,
                ELEMENT_SOURCE_ID,
                ELEMENT_STYLE_ID,
                ELEMENT_SUMMARY_ID,
                ELEMENT_TABLE_ID,
                ELEMENT_TBODY_ID,
                ELEMENT_TD_ID,
                ELEMENT_TEMPLATE_ID,
                ELEMENT_TEXTAREA_ID,
                ELEMENT_TFOOT_ID,
                ELEMENT_TH_ID,
                ELEMENT_THEAD_ID,
                ELEMENT_TITLE_ID,
                ELEMENT_TR_ID,
                ELEMENT_TRACK_ID,
                ELEMENT_UL_ID,
                ELEMENT_WBR_ID,
                ELEMENT_XMP_ID,
        };

        var h1h6 = new byte[]{
                ELEMENT_H1_ID,
                ELEMENT_H2_ID,
                ELEMENT_H3_ID,
                ELEMENT_H4_ID,
                ELEMENT_H5_ID,
                ELEMENT_H6_ID,
        };

        // check they always are consecutive and ELEMENT_ADDRESS_ID and ELEMENT_XMP_ID are first and last
        for (int i = 1; i < addressToXmpSpecial.length; i++) {
            Assert.assertEquals(addressToXmpSpecial[i], addressToXmpSpecial[i - 1] + 1);
            Assert.assertTrue(ELEMENT_ADDRESS_ID < addressToXmpSpecial[i]);
            Assert.assertTrue(ELEMENT_XMP_ID > addressToXmpSpecial[i] || ELEMENT_XMP_ID == addressToXmpSpecial[i]);
        }


        for (int i = 1; i < h1h6.length; i++) {
            Assert.assertEquals(h1h6[i], h1h6[i - 1] + 1);
            Assert.assertTrue(ELEMENT_H1_ID < h1h6[i]);
            Assert.assertTrue(ELEMENT_H6_ID > h1h6[i] || ELEMENT_H6_ID == h1h6[i]);
        }
    }
}
