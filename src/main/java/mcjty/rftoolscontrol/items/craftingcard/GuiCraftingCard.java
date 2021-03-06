package mcjty.rftoolscontrol.items.craftingcard;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.network.PacketItemNBTToServer;
import mcjty.rftoolscontrol.network.PacketTestRecipe;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.Collections;
import java.util.List;

import static mcjty.rftoolscontrol.items.craftingcard.CraftingCardContainer.*;


public class GuiCraftingCard extends GenericGuiContainer {
    public static final int WIDTH = 180;
    public static final int HEIGHT = 198;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsControl.MODID, "textures/gui/craftingcard.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFToolsControl.MODID, "textures/gui/guielements.png");

    private BlockRender[] slots = new BlockRender[1 + INPUT_SLOTS];

    public GuiCraftingCard(CraftingCardContainer container) {
        super(RFToolsControl.instance, RFToolsCtrlMessages.INSTANCE, null, container, RFToolsControl.GUI_MANUAL_CONTROL, "craftingcard");
        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(iconLocation);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        toplevel.addChild(new Label(mc, this).setText("Regular 3x3 crafting recipe").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setLayoutHint(new PositionalLayout.PositionalHint(10, 4, 160, 14)));
        toplevel.addChild(new Label(mc, this).setText("or more complicated recipes").setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setLayoutHint(new PositionalLayout.PositionalHint(10, 17, 160, 14)));
        toplevel.addChild(new Button(mc, this)
                .setText("Update")
                .setTooltips("Update the item in the output", "slot to the recipe in the", "3x3 grid")
                .addButtonEvent(parent -> {
                    RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketTestRecipe());
                })
                .setLayoutHint(new PositionalLayout.PositionalHint(110, 57, 60, 14)));

        for (int y = 0 ; y < GRID_HEIGHT ; y++) {
            for (int x = 0 ; x < GRID_WIDTH ; x++) {
                int idx = y * GRID_WIDTH + x;
                createDummySlot(toplevel, idx, new PositionalLayout.PositionalHint(x * 18 + 10, y * 18 + 37, 18, 18), createSelectionEvent(idx));
            }
        }
        createDummySlot(toplevel, INPUT_SLOTS, new PositionalLayout.PositionalHint(10 + 8 * 18, 37, 18, 18), createSelectionEvent(INPUT_SLOTS));

        updateSlots();
        window = new Window(this, toplevel);
    }

    private void createDummySlot(Panel toplevel, int idx, PositionalLayout.PositionalHint hint, BlockRenderEvent selectionEvent) {
        slots[idx] = new BlockRender(mc, this) {
            @Override
            public List<String> getTooltips() {
                Object s = slots[idx].getRenderItem();
                if (s instanceof ItemStack) {
                    ItemStack stack = (ItemStack) s;
                    List<String> list = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

                    for (int i = 0; i < list.size(); ++i) {
                        if (i == 0) {
                            list.set(i, stack.getRarity().rarityColor + list.get(i));
                        } else {
                            list.set(i, TextFormatting.GRAY + list.get(i));
                        }
                    }

                    return list;
                } else {
                    return Collections.emptyList();
                }
            }
        }
                .setHilightOnHover(true)
                .setLayoutHint(hint);
        slots[idx].addSelectionEvent(selectionEvent);
        toplevel.addChild(slots[idx]);
    }

    private void updateSlots() {
        ItemStack[] stacks = getStacks();
        if (stacks == null) {
            return;
        }
        for (int i = 0 ; i < stacks.length ; i++) {
            slots[i].setRenderItem(stacks[i]);
        }
    }


    private ItemStack[] getStacks() {
        ItemStack cardItem = mc.thePlayer.getHeldItem(EnumHand.MAIN_HAND);
        ItemStack[] stacks = null;
        if (cardItem != null && cardItem.getItem() instanceof CraftingCardItem) {
            stacks = CraftingCardItem.getStacksFromItem(cardItem);
        }
        return stacks;
    }

    private BlockRenderEvent createSelectionEvent(final int idx) {
        return new BlockRenderEvent() {
            @Override
            public void select(Widget parent) {
                ItemStack itemstack = mc.thePlayer.inventory.getItemStack();
                slots[idx].setRenderItem(itemstack);
                ItemStack[] stacks = getStacks();
                if (stacks != null) {
                    stacks[idx] = itemstack;
                    ItemStack cardItem = mc.thePlayer.getHeldItem(EnumHand.MAIN_HAND);
                    CraftingCardItem.putStacksInItem(cardItem, stacks);
                    RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketItemNBTToServer(cardItem.getTagCompound()));
                }
            }

            @Override
            public void doubleClick(Widget parent) {

            }
        };
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateSlots();
        drawWindow();
    }
}
