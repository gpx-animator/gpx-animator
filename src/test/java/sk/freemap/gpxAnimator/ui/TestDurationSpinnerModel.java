package sk.freemap.gpxAnimator.ui;

import org.junit.Test;


public class TestDurationSpinnerModel {
  

  @Test
  public void sampleTest() {
  	System.out.println("This is a sample Test.");
  }

  @Test
  public void testTest() {
  	DurationSpinnerModel d = new DurationSpinnerModel();

  	d.setValue(-100000L);

  	System.out.println(d.getValue());

  }

    	
}