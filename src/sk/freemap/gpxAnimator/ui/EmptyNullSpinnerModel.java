package sk.freemap.gpxAnimator.ui;

import javax.swing.AbstractSpinnerModel;

public class EmptyNullSpinnerModel extends AbstractSpinnerModel {

	private Integer value;

	
	@Override
	public Object getValue() {
		return value;
	}

	
	@Override
	public void setValue(final Object o) {
		if (value == null && o == null || value != null && value.equals(o)) {
			return;
		}
		
		value = o == null || o.equals(Integer.valueOf(0)) ? null : (Integer) o;
		fireStateChanged();
	}

	
	@Override
	public Object getNextValue() {
		return value == null ? 1 : (value.intValue() + 1);
	}

	
	@Override
	public Object getPreviousValue() {
		return value == null ? null : (value.intValue() - 1);
	}

}
