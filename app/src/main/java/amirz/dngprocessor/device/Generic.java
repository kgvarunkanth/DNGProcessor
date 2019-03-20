package amirz.dngprocessor.device;

import android.util.SparseArray;

import amirz.dngprocessor.Preferences;
import amirz.dngprocessor.params.ProcessParams;
import amirz.dngprocessor.params.SensorParams;
import amirz.dngprocessor.parser.TIFFTag;

class Generic implements DeviceMap.Device {
    @Override
    public boolean isModel(String model) {
        return true;
    }

    @Override
    public void sensorCorrection(SparseArray<TIFFTag> tags, SensorParams sensor) {
    }

    @Override
    public void processCorrection(SparseArray<TIFFTag> tags, ProcessParams process) {
        saturationCorrection(process.saturationMap);
    }

    void saturationCorrection(float[] saturationMap) {
        float genericMult = Preferences.global().saturationMultiplier.get();
        saturationMap[0] *= genericMult;
        saturationMap[1] *= genericMult;
        saturationMap[2] *= genericMult;
        saturationMap[3] *= genericMult;
        saturationMap[4] *= genericMult;
        saturationMap[5] *= genericMult;
        saturationMap[6] *= genericMult;
        saturationMap[7] *= genericMult;
    }
}
