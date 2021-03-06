package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftoolscontrol.api.parameters.ParameterType;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;
import mcjty.rftoolscontrol.api.parameters.Tuple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class TupleEditor extends AbstractParameterEditor {

    private TextField fieldX;
    private TextField fieldY;

    @Override
    public void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback) {
        Panel constantPanel = new Panel(mc, gui).setLayout(new HorizontalLayout());
        fieldX = new TextField(mc, gui).addTextEvent((parent, newText) -> callback.valueChanged(readValue()));
        fieldY = new TextField(mc, gui).addTextEvent((parent, newText) -> callback.valueChanged(readValue()));
        constantPanel.addChild(fieldX).addChild(fieldY);

        createEditorPanel(mc, gui, panel, callback, constantPanel, ParameterType.PAR_TUPLE);
    }

    @Override
    protected ParameterValue readConstantValue() {
        Integer x = parseIntSafe(fieldX.getText());
        Integer y = parseIntSafe(fieldY.getText());
        return ParameterValue.constant(new Tuple(x == null ? 0 : x, y == null ? 0 : y));
    }

    @Override
    protected void writeConstantValue(ParameterValue value) {
        if (value == null || value.getValue() == null) {
            fieldX.setText("");
            fieldY.setText("");
        } else {
            Tuple tuple = (Tuple) value.getValue();
            try {
                fieldX.setText(Integer.toString(tuple.getX()));
                fieldY.setText(Integer.toString(tuple.getY()));
            } catch (Exception e) {
                fieldX.setText("");
                fieldY.setText("");
            }
        }
    }
}
