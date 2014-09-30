package com.deguitard.otakrawl.services.crawler;

import org.junit.Assert;
import org.junit.Test;

public class StarKanaUrlFormatterTest {

	/**
	 * Test of the {@link StarKanaUrlFormatter#getMangaUrl(String)} method.
	 */
	@Test
	public void getMangaUrlTest() {
		Assert.assertEquals("http://starkana.com/manga/0/.hack__XXXX", StarKanaUrlFormatter.getMangaUrl(".hack//XXXX"));
		Assert.assertEquals("http://starkana.com/manga/0/C%3A_Sword_And_Cornett", StarKanaUrlFormatter.getMangaUrl("+C: Sword And Cornett"));
		Assert.assertEquals("http://starkana.com/manga/0/10", StarKanaUrlFormatter.getMangaUrl("$10"));
		Assert.assertEquals("http://starkana.com/manga/0/2x2___Shinobuden", StarKanaUrlFormatter.getMangaUrl("2x2 = Shinobuden"));
		Assert.assertEquals("http://starkana.com/manga/0/99__Love", StarKanaUrlFormatter.getMangaUrl("99% Love"));
		Assert.assertEquals("http://starkana.com/manga/0/090_%7EEko_to_Issho%7E", StarKanaUrlFormatter.getMangaUrl("090 ~Eko to Issho~"));
		Assert.assertEquals("http://starkana.com/manga/0/17_OClocks_%28One_shot%29", StarKanaUrlFormatter.getMangaUrl("17 O'Clocks (One shot)"));
		Assert.assertEquals("http://starkana.com/manga/A/All_Out%21%21", StarKanaUrlFormatter.getMangaUrl("All Out!!"));
		Assert.assertEquals("http://starkana.com/manga/0/Bungaku_Shoujo_to_Ue_Kawaku_Yuurei", StarKanaUrlFormatter.getMangaUrl("\"Bungaku Shoujo\" to Ue Kawaku Yuurei"));
		Assert.assertEquals("http://starkana.com/manga/0/000000_-_Ultra_Black", StarKanaUrlFormatter.getMangaUrl("#000000 - Ultra Black"));
		Assert.assertEquals("http://starkana.com/manga/0/Again", StarKanaUrlFormatter.getMangaUrl("+ Again"));
		Assert.assertEquals("http://starkana.com/manga/0/13nichi_wa_Kinyoubi", StarKanaUrlFormatter.getMangaUrl("13nichi wa Kin'youbi?"));
		Assert.assertEquals("http://starkana.com/manga/A/Aishite_Kudasai%2C_Sensei", StarKanaUrlFormatter.getMangaUrl("Aishite Kudasai, Sensei"));
		Assert.assertEquals("http://starkana.com/manga/A/A_B", StarKanaUrlFormatter.getMangaUrl("A+B"));
		Assert.assertEquals("http://starkana.com/manga/A/Amahara-kun", StarKanaUrlFormatter.getMangaUrl("Amahara-kun +"));
		Assert.assertEquals("http://starkana.com/manga/A/Alice_Binetsu_38__C_-_We_Are_Tsubasa_ga_Oka_D.C", StarKanaUrlFormatter.getMangaUrl("Alice Binetsu 38°C - We Are Tsubasa ga Oka D.C"));
		Assert.assertEquals("http://starkana.com/manga/0/8___1", StarKanaUrlFormatter.getMangaUrl("8♀1♂"));
		Assert.assertEquals("http://starkana.com/manga/A/Are_____Nochi_Kareshi", StarKanaUrlFormatter.getMangaUrl("Are ← Nochi Kareshi"));
		Assert.assertEquals("http://starkana.com/manga/B/Bel_x_Cha%21______________%28Doujin%29", StarKanaUrlFormatter.getMangaUrl("Bel x Cha! 草薙の剣 (Doujin)"));
		Assert.assertEquals("http://starkana.com/manga/C/Cherry_____Blossom_%28One_shot%29", StarKanaUrlFormatter.getMangaUrl("Cherry ♥ Blossom (One shot)"));
		Assert.assertEquals("http://starkana.com/manga/G/Gis__le_Alain", StarKanaUrlFormatter.getMangaUrl("Gisèle Alain"));
		Assert.assertEquals("http://starkana.com/manga/J/JOKE%3A__RS", StarKanaUrlFormatter.getMangaUrl("JOKE:ЯR'S"));
		Assert.assertEquals("http://starkana.com/manga/N/Nausica___of_the_valley_of_the_wind", StarKanaUrlFormatter.getMangaUrl("Nausicaä of the valley of the wind"));
		Assert.assertEquals("http://starkana.com/manga/R/Ral____Grad", StarKanaUrlFormatter.getMangaUrl("Ral Ω Grad"));
		Assert.assertEquals("http://starkana.com/manga/R/Re%3ABIRTH___The_Lunatic_Taker", StarKanaUrlFormatter.getMangaUrl("Re:BIRTH－The Lunatic Taker－"));
		Assert.assertEquals("http://starkana.com/manga/S/SNIPE_%28____________%29", StarKanaUrlFormatter.getMangaUrl("SNIPE (スナイプ)"));
		Assert.assertEquals("http://starkana.com/manga/T/Tokurei_Sochi_Dantai_Stella_Jogakuin_Koutouka_C___Bu", StarKanaUrlFormatter.getMangaUrl("Tokurei Sochi Dantai Stella Jogakuin Koutouka C³ Bu"));
		Assert.assertEquals("http://starkana.com/manga/W/Wei___%28Manhwa%29", StarKanaUrlFormatter.getMangaUrl("Weiß (Manhwa)"));
	}
}
