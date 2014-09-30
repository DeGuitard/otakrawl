package com.deguitard.otakrawl.services.tools;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.deguitard.otakrawl.OtakrawlTestRunner;
import com.deguitard.otakrawl.OtakrawlTestRunner.GuiceModules;
import com.deguitard.otakrawl.services.OtakrawlModule;
import com.deguitard.otakrawl.services.exceptions.NoResultFoundException;
import com.google.inject.Inject;

@RunWith(OtakrawlTestRunner.class)
@GuiceModules(OtakrawlModule.class)
public class PropertyServiceTest {

	private static final String TEST_KEY = "database.host";

	@Inject
	private PropertyService propertyService;

	@Test
	public void readPropertyTest() {
		try {
			Assert.assertNotNull(propertyService.getValue(TEST_KEY));
		} catch (NoResultFoundException e) {
			Assert.fail("Property could not be found.");
		}
	}

	@Test
	public void writePropertyTest() {
		try {
			String oldValue = propertyService.getValue(TEST_KEY);
			String newValue = "test";

			propertyService.setValue(TEST_KEY, newValue);
			Assert.assertEquals("The value of the property is different from what it should be.", newValue, propertyService.getValue(TEST_KEY));
			propertyService.setValue(TEST_KEY, oldValue);
			Assert.assertEquals("The value of the property is different from what it should be.", oldValue, propertyService.getValue(TEST_KEY));
		} catch (NoResultFoundException e) {
			Assert.fail("Old property value could not be read.");
		}
	}
}
