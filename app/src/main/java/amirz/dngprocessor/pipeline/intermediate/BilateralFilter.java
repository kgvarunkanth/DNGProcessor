package amirz.dngprocessor.pipeline.intermediate;

import amirz.dngprocessor.R;
import amirz.dngprocessor.gl.GLPrograms;
import amirz.dngprocessor.gl.Texture;
import amirz.dngprocessor.params.ProcessParams;
import amirz.dngprocessor.pipeline.Stage;
import amirz.dngprocessor.pipeline.StagePipeline;
import amirz.dngprocessor.pipeline.convert.ToIntermediate;

public class BilateralFilter extends Stage {
    private final ProcessParams mProcess;
    private Texture mBilateral;

    public BilateralFilter(ProcessParams process) {
        mProcess = process;
    }

    public Texture getBilateral() {
        return mBilateral;
    }

    @Override
    protected void execute(StagePipeline.StageMap previousStages) {
        if (mProcess.histFactor == 0f) {
            return;
        }

        GLPrograms converter = getConverter();

        Texture intermediate = previousStages.getStage(ToIntermediate.class).getIntermediate();
        int w = intermediate.getWidth();
        int h = intermediate.getHeight();

        Texture noiseTex = previousStages.getStage(NoiseMap.class).getNoiseTex();

        mBilateral = new Texture(w, h, 3, Texture.Format.Float16, null);
        try (Texture bilateralTmp = new Texture(w, h, 3, Texture.Format.Float16, null)) {
            // Pre-bilateral median filter.
            converter.setTexture("buf", intermediate);
            converter.drawBlocks(bilateralTmp);

            // 3-step bilateral filter setup.
            converter.useProgram(R.raw.stage2_3_bilateral);
            converter.seti("bufSize", w, h);
            converter.setTexture("noiseMap", noiseTex);

            // 1) Small area, strong blur.
            converter.setTexture("buf", bilateralTmp);
            converter.setf("sigma", 0.03f, 0.5f);
            converter.seti("radius", 3, 1);
            converter.drawBlocks(mBilateral);

            // 2) Medium area, medium blur.
            converter.setTexture("buf", mBilateral);
            converter.setf("sigma", 0.02f, 3f);
            converter.seti("radius", 6, 2);
            converter.drawBlocks(bilateralTmp);

            // 3) Large area, weak blur.
            converter.setTexture("buf", bilateralTmp);
            converter.setf("sigma", 0.01f, 9f);
            converter.seti("radius", 9, 3);
            converter.drawBlocks(mBilateral);
        }
    }

    @Override
    public int getShader() {
        return R.raw.stage2_3_median;
    }
}
