package amirz.dngprocessor.device;

import android.util.SparseArray;

import amirz.dngprocessor.params.SensorParams;
import amirz.dngprocessor.parser.TIFFTag;

public class OnePlus5 extends OnePlus {
    @Override
    public boolean isModel(String model) {
        return model.startsWith("ONEPLUS A5");
    }

    @Override
    public void sensorCorrection(SparseArray<TIFFTag> tags, SensorParams sensor) {
        super.sensorCorrection(tags, sensor);
        super.matrixCorrection(tags, sensor);
        sensor.opDot = true;
    }
}
