package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mrriegel.storagenetwork.gui.remote.GuiRemote;
import mrriegel.storagenetwork.gui.request.GuiRequest;
import mrriegel.storagenetwork.handler.GuiHandler;
import mrriegel.storagenetwork.helper.StackWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StacksMessage implements IMessage, IMessageHandler<StacksMessage, IMessage> {
	int size, csize, id;
	List<StackWrapper> stacks, craftableStacks;

	public StacksMessage() {
	}

	public StacksMessage(List<StackWrapper> stacks, List<StackWrapper> craftableStacks, int id) {
		super();
		this.stacks = stacks;
		this.craftableStacks = craftableStacks;
		this.size = stacks.size();
		this.csize = craftableStacks.size();
		this.id = id;
	}

	@Override
	public IMessage onMessage(final StacksMessage message, final MessageContext ctx) {
		IThreadListener mainThread = Minecraft.getMinecraft();
		mainThread.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				if (message.id == GuiHandler.REMOTE && Minecraft.getMinecraft().currentScreen instanceof GuiRemote) {
					GuiRemote gui = (GuiRemote) Minecraft.getMinecraft().currentScreen;
					gui.stacks = message.stacks;
				} else if (message.id == GuiHandler.REQUEST && Minecraft.getMinecraft().currentScreen instanceof GuiRequest) {
					GuiRequest gui = (GuiRequest) Minecraft.getMinecraft().currentScreen;
					gui.stacks = message.stacks;
					gui.craftableStacks = message.craftableStacks;
				}

			}
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.size = buf.readInt();
		this.csize = buf.readInt();
		this.id = buf.readInt();
		stacks = new ArrayList<StackWrapper>();
		for (int i = 0; i < size; i++) {
			stacks.add(StackWrapper.loadStackWrapperFromNBT(ByteBufUtils.readTag(buf)));
		}
		craftableStacks = new ArrayList<StackWrapper>();
		for (int i = 0; i < csize; i++) {
			craftableStacks.add(StackWrapper.loadStackWrapperFromNBT(ByteBufUtils.readTag(buf)));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.size);
		buf.writeInt(this.csize);
		buf.writeInt(this.id);
		for (StackWrapper w : stacks) {
			NBTTagCompound compound = new NBTTagCompound();
			w.writeToNBT(compound);
			ByteBufUtils.writeTag(buf, compound);
		}
		for (StackWrapper w : craftableStacks) {
			NBTTagCompound compound = new NBTTagCompound();
			w.writeToNBT(compound);
			ByteBufUtils.writeTag(buf, compound);
		}
	}
}
