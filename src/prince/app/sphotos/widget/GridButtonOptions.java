package prince.app.sphotos.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

public class GridButtonOptions extends ImageButton {

    public GridButtonOptions(Context context) {
        super(context);
    }

    public GridButtonOptions(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridButtonOptions(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setPressed(boolean pressed) {
        if (pressed && getParent() instanceof View && ((View) getParent()).isPressed()) {
            return;
        }
        super.setPressed(pressed);
    }

}

