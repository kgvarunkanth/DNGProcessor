package amirz.dngprocessor.pipeline.intermediate;

import amirz.dngprocessor.R;
import amirz.dngprocessor.gl.GLPrograms;
import amirz.dngprocessor.gl.Texture;
import amirz.dngprocessor.params.ProcessParams;
import amirz.dngprocessor.pipeline.Stage;
import amirz.dngprocessor.pipeline.StagePipeline;
import amirz.dngprocessor.pipeline.convert.ToIntermediate;

import static android.opengl.GLES20.*;

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

        mBilateral = new Texture(w, h, 3, Texture.Format.Float16, null);
        try (Texture bilateralTmp = new Texture(w, h, 3, Texture.Format.Float16, null)) {
            // Pre-bilateral median filter.
            intermediate.bind(GL_TEXTURE0);
            converter.seti("buf", 0);
            bilateralTmp.setFrameBuffer();
            converter.drawBlocks(w, h);

            // Bilateral filter setup.
            converter.useProgram(R.raw.stage2_1_bilateral);
            converter.seti("buf", 0);
            converter.seti("bufSize", w, h);

            // 1) Small area, strong blur.
            bilateralTmp.bind(GL_TEXTURE0);
            mBilateral.setFrameBuffer();
            converter.setf("sigma", 0.15f, 1.5f);
            converter.seti("radius", 7, 1);
            converter.drawBlocks(w, h);

            // 2) Medium area, medium blur.
            mBilateral.bind(GL_TEXTURE0);
            bilateralTmp.setFrameBuffer();
            converter.setf("sigma", 0.05f, 4.5f);
            converter.seti("radius", 14, 2);
            converter.drawBlocks(w, h);

            // 3) Large area, weak blur.
            bilateralTmp.bind(GL_TEXTURE0);
            mBilateral.setFrameBuffer();
            converter.setf("sigma", 0.02f, 13.5f);
            converter.seti("radius", 21, 3);
            converter.drawBlocks(w, h);
        }
    }

    @Override
    public int getShader() {
        return R.raw.stage2_1_median;
    }
}
