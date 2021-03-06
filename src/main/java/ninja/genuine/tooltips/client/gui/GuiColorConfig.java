package ninja.genuine.tooltips.client.gui;

import java.io.IOException;
import java.util.regex.Pattern;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import ninja.genuine.tooltips.WorldTooltips;
import ninja.genuine.tooltips.client.config.Config;

public class GuiColorConfig extends GuiScreen {

	private GuiScreen parent;
	private GuiTextField textboxOutline, textboxBackground;
	private GuiColorButton outlineButton, backgroundButton;
	private String strOutlineOrig, strBackgroundOrig;
	int editing = 0;

	public GuiColorConfig(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		strOutlineOrig = Config.getInstance().getOutline().getString();
		strBackgroundOrig = Config.getInstance().getBackground().getString();
		ScaledResolution sr = new ScaledResolution(mc);
		GuiButton back = new GuiButton(0, sr.getScaledWidth() / 2 - 100, sr.getScaledHeight() - 30, 200, 20, I18n.format("gui.cancel"));
		GuiButton done = new GuiButton(1, sr.getScaledWidth() / 2 - 100, sr.getScaledHeight() - 55, 200, 20, I18n.format("gui.done"));
		outlineButton = new GuiColorButton(5, sr.getScaledWidth() / 2 + 130, sr.getScaledHeight() / 2 - 80, Config.getInstance().getOutline());
		backgroundButton = new GuiColorButton(6, sr.getScaledWidth() / 2 + 130, sr.getScaledHeight() / 2 - 50, Config.getInstance().getBackground());
		textboxOutline = new GuiTextField(10, mc.fontRenderer, sr.getScaledWidth() / 2 + 50, sr.getScaledHeight() / 2 - 80, 100, 20);
		textboxBackground = new GuiTextField(10, mc.fontRenderer, sr.getScaledWidth() / 2 + 50, sr.getScaledHeight() / 2 - 50, 100, 20);
		textboxOutline.setText(Config.getInstance().getOutline().getString());
		textboxOutline.setMaxStringLength(10);
		textboxOutline.setValidator((input) -> Pattern.compile("(0[x#])?[0-9a-fA-F]{0,8}").asPredicate().test(input));
		textboxBackground.setText(Config.getInstance().getBackground().getString());
		textboxBackground.setMaxStringLength(10);
		textboxBackground.setValidator((input) -> Pattern.compile("(0[x#])?[0-9a-fA-F]{0,8}").asPredicate().test(input));
		addButton(outlineButton);
		addButton(backgroundButton);
		addButton(done);
		addButton(back);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		outlineButton.update(textboxOutline.getText());
		backgroundButton.update(textboxBackground.getText());
		sync();
		drawDefaultBackground();
		ScaledResolution sr = new ScaledResolution(mc);
		fontRenderer.drawString(Config.getInstance().getOutline().getName(), sr.getScaledWidth() / 2 - 140, sr.getScaledHeight() / 2 - 75, 0xFFFFFFFF);
		fontRenderer.drawString(Config.getInstance().getBackground().getName(), sr.getScaledWidth() / 2 - 140, sr.getScaledHeight() / 2 - 45, 0xFFFFFFFF);
		fontRenderer.drawSplitString("Remember, you must enable 'Override Outline Color' to display the custom outline color.", sr.getScaledWidth() / 2 - 140, sr.getScaledHeight() / 2 + 0, 300, 0xFF808080);
		textboxOutline.drawTextBox();
		textboxBackground.drawTextBox();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		textboxOutline.updateCursorCounter();
		textboxBackground.updateCursorCounter();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		textboxOutline.textboxKeyTyped(typedChar, keyCode);
		textboxBackground.textboxKeyTyped(typedChar, keyCode);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textboxOutline.mouseClicked(mouseX, mouseY, mouseButton);
		textboxBackground.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void sync() {
		try {
			Config.getInstance().getOutline().set(textboxOutline.getText());
		} catch (Exception e) {}
		try {
			Config.getInstance().getBackground().set(textboxBackground.getText());
		} catch (Exception e) {}
		outlineButton.update(textboxOutline.getText());
		backgroundButton.update(textboxBackground.getText());
		WorldTooltips.instance.sync();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			try {
				Config.getInstance().getOutline().set(strOutlineOrig);
			} catch (Exception e) {}
			try {
				Config.getInstance().getBackground().set(strBackgroundOrig);
			} catch (Exception e) {}
			WorldTooltips.instance.sync();
			mc.displayGuiScreen(parent);
		} else if (button.id == 1) {
			sync();
			mc.displayGuiScreen(parent);
		} else if (button.id == 5) {
			mc.displayGuiScreen(new GuiColorPicker(this, textboxOutline, strOutlineOrig));
			editing = 1;
		} else if (button.id == 6) {
			mc.displayGuiScreen(new GuiColorPicker(this, textboxBackground, strBackgroundOrig));
			editing = 2;
		}
	}
}
