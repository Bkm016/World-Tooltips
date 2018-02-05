package ninja.genuine.tooltips.client;

import java.lang.reflect.Field;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ninja.genuine.tooltips.Constants;
import ninja.genuine.tooltips.client.config.Config;

public class RenderEvent {

	private Minecraft mc;
	private Tooltip tooltip;
	private EntityItem entity;

	public RenderEvent() {}

	public void post() {
		mc = Minecraft.getMinecraft();
	}

	public void sync() {
		if (tooltip != null)
			tooltip.sync();
	}

	@SubscribeEvent
	public void render(final RenderWorldLastEvent event) {
		EntityItem tmp = getMouseOver(mc, event.getPartialTicks());
		if (tmp == null)
			return;
		entity = tmp;
		if (tooltip == null || tooltip.getEntity() != entity)
			tooltip = new Tooltip(Minecraft.getMinecraft().player, entity);
		if (tooltip == null)
			return;
		tooltip.renderTooltip3D(mc, event.getPartialTicks());
	}

	@SubscribeEvent
	public void render(final RenderGameOverlayEvent.Post event) {
		// TODO Let's make it a choice to do 2D or 3D tooltips. Just need to make a nice anchoring gui first.
		// renderer.renderTooltip2D(mc, item, generateTooltip(mc, mc.player, item.getEntityItem()), event.getPartialTicks());
	}

	public static EntityItem getMouseOver(Minecraft mc, float partialTicks) {
		mc.mcProfiler.startSection(Constants.MODID);
		Entity viewer = mc.getRenderViewEntity();
		int range = Config.getInstance().getMaxDistance();
		Vec3d eyes = viewer.getPositionEyes(partialTicks);
		Vec3d look = viewer.getLook(partialTicks);
		Vec3d view = eyes.addVector(look.x * range, look.y * range, look.z * range);
		double distance = 0;
		EntityItem out = null;
		List<EntityItem> list = mc.world.getEntitiesWithinAABB(EntityItem.class, viewer.getEntityBoundingBox().expand(look.x * range, look.y * range, look.z * range).grow(1F, 1F, 1F));
		for (int i = 0; i < list.size(); i++) {
			EntityItem entity = list.get(i);
			AxisAlignedBB aabb = entity.getEntityBoundingBox().offset(0, 0.25, 0).grow(entity.getCollisionBorderSize() + 0.1);
			RayTraceResult ray = aabb.calculateIntercept(eyes, view);
			if (aabb.contains(eyes)) {
				if (distance > 0) {
					out = entity;
					distance = 0;
				}
			} else if (ray != null) {
				double d = eyes.distanceTo(ray.hitVec);
				if (d < distance || distance == 0) {
					out = entity;
					distance = d;
				}
			}
		}
		mc.mcProfiler.endSection();
		
		// Support for HolographicDisplays
		if (out != null && out.getItem().hasTagCompound()) {
			if (out.getItem().getTagCompound().hasKey("display")) {
				NBTTagCompound nbt = out.getItem().getTagCompound().getCompoundTag("display");
				if (nbt.hasKey("Lore")) {
                    NBTTagList lore = nbt.getTagList("Lore", 8);
                    if (lore.tagCount() == 1) {
                    	try {
                    		Double.valueOf(lore.get(0).toString().replaceAll("\"|��0", ""));
                    		return null;
                    	}
                    	catch (Exception e) {
							//
						}
                    }
                }
			}
		}
		return out;
	}
}
