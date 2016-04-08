package mrriegel.storagenetwork.init;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.blocks.BlockAnnexer;
import mrriegel.storagenetwork.blocks.BlockContainer;
import mrriegel.storagenetwork.blocks.BlockCover;
import mrriegel.storagenetwork.blocks.BlockCrafter;
import mrriegel.storagenetwork.blocks.BlockFKabel;
import mrriegel.storagenetwork.blocks.BlockFRequest;
import mrriegel.storagenetwork.blocks.BlockFannexer;
import mrriegel.storagenetwork.blocks.BlockIndicator;
import mrriegel.storagenetwork.blocks.BlockItemBox;
import mrriegel.storagenetwork.blocks.BlockKabel;
import mrriegel.storagenetwork.blocks.BlockMaster;
import mrriegel.storagenetwork.blocks.BlockRequest;
import mrriegel.storagenetwork.tile.TileAnnexer;
import mrriegel.storagenetwork.tile.TileContainer;
import mrriegel.storagenetwork.tile.TileCrafter;
import mrriegel.storagenetwork.tile.TileFRequest;
import mrriegel.storagenetwork.tile.TileFannexer;
import mrriegel.storagenetwork.tile.TileIndicator;
import mrriegel.storagenetwork.tile.TileItemBox;
import mrriegel.storagenetwork.tile.TileKabel;
import mrriegel.storagenetwork.tile.TileMaster;
import mrriegel.storagenetwork.tile.TileRequest;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder(value = StorageNetwork.MODID)
public class ModBlocks {
	public static final Block master = new BlockMaster();
	public static final Block request = new BlockRequest();
	public static final Block frequest = new BlockFRequest();
	public static final Block kabel = new BlockKabel().setUnlocalizedName(StorageNetwork.MODID + ":kabel");
	public static final Block storageKabel = new BlockKabel().setUnlocalizedName(StorageNetwork.MODID + ":storageKabel");
	public static final Block exKabel = new BlockKabel().setUnlocalizedName(StorageNetwork.MODID + ":exKabel");
	public static final Block imKabel = new BlockKabel().setUnlocalizedName(StorageNetwork.MODID + ":imKabel");
	public static final Block vacuumKabel = new BlockKabel().setUnlocalizedName(StorageNetwork.MODID + ":vacuumKabel");
	public static final Block fstorageKabel = new BlockFKabel().setUnlocalizedName(StorageNetwork.MODID + ":fstorageKabel");
	public static final Block fexKabel = new BlockFKabel().setUnlocalizedName(StorageNetwork.MODID + ":fexKabel");
	public static final Block fimKabel = new BlockFKabel().setUnlocalizedName(StorageNetwork.MODID + ":fimKabel");
	public static final Block cover = new BlockCover();
	public static final Block container = new BlockContainer();
	public static final Block crafter = new BlockCrafter();
	public static final Block indicator = new BlockIndicator();
	public static final Block annexer = new BlockAnnexer();
	public static final Block fannexer = new BlockFannexer();
	public static final Block itemBox = new BlockItemBox();

	public static void init() {
		GameRegistry.registerBlock(master, "master");
		GameRegistry.registerBlock(request, "request");
		GameRegistry.registerBlock(frequest, "frequest");
		GameRegistry.registerBlock(kabel, "kabel");
		GameRegistry.registerBlock(storageKabel, "storageKabel");
		GameRegistry.registerBlock(exKabel, "exKabel");
		GameRegistry.registerBlock(imKabel, "imKabel");
		GameRegistry.registerBlock(vacuumKabel, "vacuumKabel");
		GameRegistry.registerBlock(fstorageKabel, "fstorageKabel");
		GameRegistry.registerBlock(fexKabel, "fexKabel");
		GameRegistry.registerBlock(fimKabel, "fimKabel");
		GameRegistry.registerBlock(cover, "cover");
		GameRegistry.registerBlock(indicator, "indicator");
		// GameRegistry.registerBlock(container, "container");
		// GameRegistry.registerBlock(crafter, "crafter");
		GameRegistry.registerBlock(annexer, "annexer");
		GameRegistry.registerBlock(fannexer, "fannexer");
		// GameRegistry.registerBlock(itemBox, "itemBox");

		GameRegistry.registerTileEntity(TileKabel.class, "tileKabel");
		GameRegistry.registerTileEntity(TileMaster.class, "tileMaster");
		GameRegistry.registerTileEntity(TileRequest.class, "tileRequest");
		GameRegistry.registerTileEntity(TileFRequest.class, "tileFRequest");
		GameRegistry.registerTileEntity(TileContainer.class, "tileContainer");
		GameRegistry.registerTileEntity(TileCrafter.class, "tileCrafter");
		GameRegistry.registerTileEntity(TileIndicator.class, "tileIndicator");
		GameRegistry.registerTileEntity(TileAnnexer.class, "tileAnnexer");
		GameRegistry.registerTileEntity(TileFannexer.class, "tileFannexer");
		GameRegistry.registerTileEntity(TileItemBox.class, "tileItemBox");

	}

}
