/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class ButtonBarSkin extends BehaviorSkinBase<ButtonBar, BehaviorBase<ButtonBar>> {
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/

    private static String CATEGORIZED_TYPES = "LRHEYNXBIACO";
    
    // represented as a ButtonType
    public static String BUTTON_TYPE_PROPERTY     = "controlfx.button.type";
    
    // allows to exclude button from uniform resizing
    public static String BUTTON_SIZE_INDEPENDENCE = "controlfx.button.size.indepenence";
    
    
    /**************************************************************************
     * 
     * fields
     * 
     **************************************************************************/
    
    private HBox hbox;
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/

    public ButtonBarSkin(final ButtonBar control) {
        super(control, new BehaviorBase<>(control));
        
        // TODO: the gap has to be OS dependent
        this.hbox = new HBox(10) {
            @Override
            protected void layoutChildren() {
                // has to be called first or layout is not correct somtimes 
                resizeButtons();
                super.layoutChildren();
            }
        };
        this.hbox.setAlignment(Pos.CENTER);
        getChildren().add(hbox);
        
        layoutButtons();
        
        control.getButtons().addListener( new ListChangeListener<ButtonBase>() {
            @Override public void onChanged(ListChangeListener.Change<? extends ButtonBase> change) {
                layoutButtons();
            }
        });
        
        registerChangeListener(control.buttonOrderProperty(), "BUTTON_ORDER");
        registerChangeListener(control.buttonMinWidthProperty(), "BUTTON_MIN_WIDTH");
        registerChangeListener(control.buttonUniformSizeProperty(), "BUTTON_UNIFORM_SIZE");
    }
    
    
    /**************************************************************************
     * 
     * Overriding public API
     * 
     **************************************************************************/
    
    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if ("BUTTON_ORDER".equals(p)) {
            layoutButtons();
        } else if ("BUTTON_MIN_WIDTH".equals(p)) {
            layoutButtons();
        } else if ("BUTTON_UNIFORM_SIZE".equals(p)) {
//            layoutButtons();
            resizeButtons();
        }
    }
    
    
    
    /**************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/
    
    private void layoutButtons() {
        final ButtonBar buttonBar = getSkinnable();
        final List<? extends ButtonBase> buttons = buttonBar.getButtons();
        final double buttonMinWidth = buttonBar.getButtonMinWidth();
        
        Map<String, List<ButtonBase>> buttonMap = buildButtonMap(buttons);
        SpacerType preparedSpacer = SpacerType.NONE;
        String buttonOrderStr = getSkinnable().getButtonOrder();
        
        if (buttonOrderStr == null || buttonOrderStr.isEmpty()) {
            throw new IllegalStateException("ButtonBar buttonOrder string can not be null or empty");
        }
         
        char[] buttonOrder = buttonOrderStr.toCharArray();
        hbox.getChildren().clear();
        
        for (int i = 0; i < buttonOrder.length; i++) {
            char type = buttonOrder[i];
            if (type == '+') {
                preparedSpacer = preparedSpacer.replace(SpacerType.DYNAMIC);
            } else if (type == '_') {
               preparedSpacer = preparedSpacer.replace(SpacerType.FIXED);
            } else {
                List<ButtonBase> buttonList = buttonMap.get(String.valueOf(type).toUpperCase());
                if (buttonList != null) {
                    Node spacer = preparedSpacer.create();
                    if (spacer != null) {
                        hbox.getChildren().add(spacer);
                        preparedSpacer = preparedSpacer.replace(SpacerType.NONE);
                    }
                    
                    for (ButtonBase btn: buttonList) {
                        btn.setMinWidth(buttonMinWidth);
                        btn.setMaxWidth(Double.MAX_VALUE);
                        
                        hbox.getChildren().add(btn);
                        HBox.setHgrow(btn, Priority.NEVER);
                    }
                } 
            }
        }
        
    }
    
    // Button sizing. If buttonUniformSize is true button size = max(buttonMinSize, max(all button pref sizes))
    // otherwise button size = max(buttonBar.buttonMinSize, button pref size)
    private void resizeButtons() {
        final ButtonBar buttonBar = getSkinnable();
        double buttonMinWidth = buttonBar.getButtonMinWidth();
        final List<? extends ButtonBase> buttons = buttonBar.getButtons();
        
        // determine the widest button
        double widest = buttonMinWidth;
        if (buttonBar.isButtonUniformSize()) {
            for (ButtonBase button : buttons) {
                if ( !isButtonIndependent(button)) {
                   widest = Math.max(button.prefWidth(-1), widest);
                }
            }
        }
        
        // set the width of all buttons
        for (ButtonBase button : buttons) {
            
            if (buttonBar.isButtonUniformSize()) {
                if ( !isButtonIndependent(button)) { 
                    button.setPrefWidth(widest);
                }
            } else if (buttonMinWidth > 0){
                button.setMinWidth(buttonMinWidth);
            } else {
                button.setMinWidth(0);
                button.setPrefWidth(Region.USE_COMPUTED_SIZE);
            }
//            double width = buttonBar.isButtonUniformSize()? widest : Math.max(buttonMinWidth, button.prefWidth(-1));
//            button.setMinWidth( width );
//            button.setMaxWidth( width );
//            button.setPrefWidth(width);
        }
    }
    
    private Boolean isButtonIndependent( ButtonBase btn ) {
        return Boolean.TRUE.equals(btn.getProperties().get(BUTTON_SIZE_INDEPENDENCE));
    }
    
    private String getButtonType(ButtonBase btn) {
        ButtonType buttonType =  (ButtonType) btn.getProperties().get(BUTTON_TYPE_PROPERTY);
        
        if (buttonType == null) {
            // TODO throw exception or just assume it is ButtonType.OTHER?
            //throw new IllegalStateException("Button '" + btn.getText() + "' does not have a ButtonType specified");
            buttonType = ButtonType.OTHER;
        }
        
        String typeCode = buttonType.getTypeCode();
        typeCode = typeCode.length() > 0? typeCode.substring(0,1): "";
        return CATEGORIZED_TYPES.contains(typeCode.toUpperCase())? typeCode : ButtonType.OTHER.getTypeCode(); 
    }
    
    private Map<String, List<ButtonBase>> buildButtonMap( List<? extends ButtonBase> buttons ) {
        Map<String, List<ButtonBase>> buttonMap = new HashMap<>();
        for (ButtonBase btn : buttons) {
            if ( btn == null ) continue;
            String type =  getButtonType(btn); 
            List<ButtonBase> typedButtons = buttonMap.get(type);
            if ( typedButtons == null ) {
                typedButtons = new ArrayList<ButtonBase>();
                buttonMap.put(type, typedButtons);
            }
            typedButtons.add( btn );
        }
        return buttonMap;
    }
    
    
    
    /**************************************************************************
     * 
     * Support classes / enums
     * 
     **************************************************************************/
    
    private enum SpacerType {
        FIXED {
            @Override public Node create() {
                Region spacer = new Region();
                spacer.setMinWidth(10);
                HBox.setHgrow(spacer, Priority.NEVER);
                return spacer;
            }
            
            @Override public SpacerType replace( SpacerType type ) {
                return type == NONE || type == DYNAMIC? type: this;
            }
        },
        DYNAMIC {
            @Override public Node create() {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                return spacer;
            }
            
            @Override public SpacerType replace( SpacerType type ) {
                return type == NONE? type: this;
            }
        },
        NONE {
            @Override public SpacerType replace( SpacerType type ) {
                return type == FIXED || type == DYNAMIC? type: this;
            }
        };
        
        public abstract SpacerType replace( SpacerType type );
        
        public Node create() {
            return null;
        }
    }
}