package uvm.refimpl.itpr;

import java.util.ArrayList;
import java.util.List;

public class StructBox extends ValueBox {
    private List<ValueBox> boxes = new ArrayList<ValueBox>();

    public ValueBox getBox(int i) {
        return boxes.get(i);
    }

    public void addBox(ValueBox box) {
        boxes.add(box);
    }
}
