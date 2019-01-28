package no.nav.vedtak.sikkerhetsfilter;

import org.junit.Assert;
import org.junit.Test;

public class SimpelHvitvaskerTest {
    private String input_UfarligTekst = "Helt ufarilig string med diverse noe < > ping plongæøåÆØÅ";
    private String input_scriptTekst = "<Script kiddi=pingo> Noe helt greit <script/>";
    private String input_TagTekst = "<tag> tagging </tag>";

    //skal brukes som default vasking
    private String resultatAvKunBokstaverHvitvasking_UfarligTekst = "Helt ufarilig string med diverse noe _ _ ping plongæøåÆØÅ";
    private String resultatAvKunBokstaverHvitvasking_ScriptTekst = "_Script kiddi_pingo_ Noe helt greit _script__";
    private String resultatAvKunBokstaverHvitvasking_TagTekst = "_tag_ tagging __tag_";

    //skal brukes for å vaske query string.
    private String resultatAvBokstaverOgVanligeTegn_UfarligTekst = "Helt ufarilig string med diverse noe _ _ ping plongæøåÆØÅ";
    private String resultatAvBokstaverOgVanligeTegn_ScriptTekst = "_Script kiddi=pingo_ Noe helt greit _script__";
    private String resultatAvBokstaverOgVanligeTegn_TagTekst = "_tag_ tagging __tag_";

    //skal brukes for å vaske cookie.
    private String resultatAvCookie_UfarligTekst = "Helt_ufarilig_string_med_diverse_noe_<_>_ping_plong______";
    private String resultatAvCookie_ScriptTekst = "<Script_kiddi=pingo>_Noe_helt_greit_<script/>";
    private String resultatAvCookie_TagTekst = "<tag>_tagging_</tag>";

    @Test
    public void testRestriktivHvitvaskUfarligTekst() {
        String sanitizedString = SimpelHvitvasker.hvitvaskKunBokstaver(input_UfarligTekst);
        Assert.assertEquals("Not sanitized correctry", resultatAvKunBokstaverHvitvasking_UfarligTekst, sanitizedString);
    }

    @Test
    public void testRestriktivHvitvaskingScriptTekst() {
        String sanitizedString = SimpelHvitvasker.hvitvaskKunBokstaver(input_scriptTekst);
        Assert.assertEquals("Not sanitized correctry", resultatAvKunBokstaverHvitvasking_ScriptTekst, sanitizedString);
    }

    @Test
    public void testRestriktivHvitvaskTagTekst() {
        String sanitizedString = SimpelHvitvasker.hvitvaskKunBokstaver(input_TagTekst);
        Assert.assertEquals("Not sanitized correctry", resultatAvKunBokstaverHvitvasking_TagTekst, sanitizedString);
    }

    @Test
    public void testBokstaverOgVanligeTegnHvitvaskUfarligTekst() {
        String sanitizedString = SimpelHvitvasker.hvitvaskBokstaverOgVanligeTegn(input_UfarligTekst);
        Assert.assertEquals("Not sanitized correctry", resultatAvBokstaverOgVanligeTegn_UfarligTekst, sanitizedString);
    }

    @Test
    public void testBokstaverOgVanligeTegnHvitvaskingScriptTekst() {
        String sanitizedString = SimpelHvitvasker.hvitvaskBokstaverOgVanligeTegn(input_scriptTekst);
        Assert.assertEquals("Not sanitized correctry", resultatAvBokstaverOgVanligeTegn_ScriptTekst, sanitizedString);
    }

    @Test
    public void testBokstaverOgVanligeTegnHvitvaskTagTekst() {
        String sanitizedString = SimpelHvitvasker.hvitvaskBokstaverOgVanligeTegn(input_TagTekst);
        Assert.assertEquals("Not sanitized correctry", resultatAvBokstaverOgVanligeTegn_TagTekst, sanitizedString);
    }

    @Test
    public void testCookieHvitvaskUfarligTekst() {
        String sanitizedString = SimpelHvitvasker.hvitvaskCookie(input_UfarligTekst);
        Assert.assertEquals("Not sanitized correctry", resultatAvCookie_UfarligTekst, sanitizedString);
    }

    @Test
    public void testCookieHvitvaskingScriptTekst() {
        String sanitizedString = SimpelHvitvasker.hvitvaskCookie(input_scriptTekst);
        Assert.assertEquals("Not sanitized correctry", resultatAvCookie_ScriptTekst, sanitizedString);
    }

    @Test
    public void testCookieHvitvaskTagTekst() {
        String sanitizedString = SimpelHvitvasker.hvitvaskCookie(input_TagTekst);
        Assert.assertEquals("Not sanitized correctry", resultatAvCookie_TagTekst, sanitizedString);
    }

}
