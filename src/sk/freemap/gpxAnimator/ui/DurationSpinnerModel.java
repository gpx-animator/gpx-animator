/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package sk.freemap.gpxAnimator.ui;

import javax.swing.AbstractSpinnerModel;

public class DurationSpinnerModel extends AbstractSpinnerModel {

	private static final long serialVersionUID = -2807094520076994683L;
	
	private long duration;
	
	
	public enum Field {
		MILLISECOND,
		SECOND,
		MINUTE,
		HOUR,
		DAY
	}
	
	private Field field = Field.SECOND;
	
	
	@Override
	public Object getValue() {
		return duration;
	}
	

	@Override
	public void setValue(final Object value) {
		if (!Long.valueOf(duration).equals(value)) {
			duration = (Long) value;
			fireStateChanged();
		}
	}
	

	@Override
	public Object getNextValue() {
		return duration + getDiffMs();
	}
	

	@Override
	public Object getPreviousValue() {
		return duration - getDiffMs();
	}
	
	
	public void setField(final Field field) {
		this.field = field;
	}
	

	private long getDiffMs() {
		long add = 1;
		switch (field) {
		case DAY:
			add *= 24;
		case HOUR:
			add *= 60;
		case MINUTE:
			add *= 60;
		case SECOND:
			add *= 1000;
		case MILLISECOND:
			break;
		default:
			throw new AssertionError();
		}
		return add;
	}
	
}
