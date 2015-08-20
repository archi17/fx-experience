package org.comtel2000.keyboard.control;

/*
 * #%L
 * fx-onscreen-keyboard
 * %%
 * Copyright (C) 2014 - 2015 comtel2000
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the comtel2000 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.util.Collection;

import org.slf4j.LoggerFactory;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;

class MultiKeyButton extends KeyButton {

	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MultiKeyButton.class);

	private MultiKeyPopup context;

	private final Collection<String> styles;

	private final DoubleProperty scaleProperty;

	MultiKeyButton(DoubleProperty scaleProperty, Collection<String> styles) {
		super();
		getStyleClass().add("multi-button");
		this.styles = styles;
		this.scaleProperty = scaleProperty;
	}

	@Override
	protected void initEventListener(long delay) {

		buttonDelay = new Timeline(new KeyFrame(new Duration(delay), event -> fireLongPressed()));

		setOnDragDetected(e -> {
			logger.trace("{} drag detected", getKeyCode());
			if (buttonDelay.getStatus().equals(Status.RUNNING) && buttonDelay.getCurrentRate() > 0) {
				buttonDelay.stop();
				fireLongPressed();
			}
			e.consume();
		});

		setOnMouseClicked(event -> {
			logger.trace("{} clicked: {}", getKeyCode(), buttonDelay.getCurrentRate());

			if (event.getButton().equals(MouseButton.PRIMARY)) {
				if (buttonDelay.getStatus().equals(Status.RUNNING)) {
					buttonDelay.stop();
					fireShortPressed();
				}
			}
			setFocused(false);
			event.consume();
		});

		setOnMousePressed(event -> {
			logger.trace("{} pressed: {}", getKeyCode(), buttonDelay.getCurrentRate());
			if (event.getButton().equals(MouseButton.PRIMARY)) {
				buttonDelay.playFromStart();
			}
			event.consume();
		});

	}

	private MultiKeyPopup getContext() {
		if (context == null) {
			context = new MultiKeyPopup();
			context.getStylesheets().setAll(styles);
			context.setOnHidden(event -> {
				getParent().getParent().setEffect(null);
				getParent().getParent().setDisable(false);
			});
			setOnLongPressed(event -> {

				// getParent().getParent().setEffect(new BoxBlur());
				getParent().getParent().setDisable(true);
				setFocused(false);
				context.show((Node) event.getSource(), scaleProperty.get());
			});

		}
		return context;
	}

	@Override
	public void addExtKeyCode(int extKeyCode, String label) {
		KeyButton button = new ShortPressKeyButton();
		button.setText(label);
		button.setKeyCode(extKeyCode);

		if (getStyleClass() != null) {
			button.getStyleClass().addAll(getStyleClass());
		} else {
			button.setId("key-context-button");
		}
		button.setFocusTraversable(false);

		button.setPrefWidth(this.getPrefWidth());
		button.setPrefHeight(this.getPrefHeight());

		button.setOnShortPressed(getOnShortPressed());

		getContext().addButton(button);
	}

}
