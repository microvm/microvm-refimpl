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

    public int size() {
        return boxes.size();
    }

    @Override
    public void copyValue(ValueBox _that) {
        StructBox that = (StructBox) _that;
        for (int i = 0; i < boxes.size(); i++) {
            this.getBox(i).copyValue(that.getBox(i));
        }
    }
}
