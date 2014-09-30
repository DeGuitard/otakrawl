package com.deguitard.otakrawl.services.imprt;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.deguitard.otakrawl.OtakrawlTestRunner;
import com.deguitard.otakrawl.OtakrawlTestRunner.GuiceModules;
import com.deguitard.otakrawl.services.OtakrawlModule;
import com.google.inject.Inject;

@RunWith(OtakrawlTestRunner.class)
@GuiceModules(OtakrawlModule.class)
public class ImportServiceTest {

	@Inject
	private ImportService importService;

	@Test
	public void fullImportTest() {
		importService.fullImport();
	}

	@Test
	public void updateTest() {
		importService.updateImport();
	}

	@Test
	public void updateSuggestionsTest() {
		importService.updateSuggestions();
	}
}
