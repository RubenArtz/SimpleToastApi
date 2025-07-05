package ruben_artz.toast.libs.nms.v1_21_R3;

import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ruben_artz.toast.libs.nms.EToastType;
import ruben_artz.toast.libs.nms.IToastWrapper;

import java.util.*;

public class ToastImpl implements IToastWrapper {

    @Override
    public void sendToast(ItemStack icon, Player player, String title, EToastType toastType, String namespace, String path) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.world.item.ItemStack iconNMS = CraftItemStack.asNMSCopy(new ItemStack(Material.AIR));
        if (icon != null) {
            iconNMS = CraftItemStack.asNMSCopy(icon);
        }
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        Optional<DisplayInfo> displayInfo = Optional.of(new DisplayInfo(iconNMS, Objects.requireNonNull(Component.Serializer.fromJson(title, craftServer.getServer().registryAccess())), Component.literal("."), Optional.empty(), AdvancementType.valueOf(toastType.toString()), true, false, true));
        AdvancementRewards advancementRewards = AdvancementRewards.EMPTY;
        Optional<ResourceLocation> id = Optional.of(ResourceLocation.fromNamespaceAndPath(namespace, path));
        Criterion<ImpossibleTrigger.TriggerInstance> impossibleTrigger = new Criterion<>(new ImpossibleTrigger(), new ImpossibleTrigger.TriggerInstance());
        HashMap<String, Criterion<?>> criteria = new HashMap<>() {{
            put("impossible", impossibleTrigger);
        }};
        List<List<String>> requirements = new ArrayList<>() {{
            add(new ArrayList<>() {{
                add("impossible");
            }});
        }};
        AdvancementRequirements advancementRequirements = new AdvancementRequirements(requirements);
        Advancement advancement = new Advancement(Optional.empty(), displayInfo, advancementRewards, criteria, advancementRequirements, false);
        Map<ResourceLocation, AdvancementProgress> advancementsToGrant = new HashMap<>();
        AdvancementProgress advancementProgress = new AdvancementProgress();
        advancementProgress.update(advancementRequirements);
        advancementProgress.getCriterion("impossible").grant();
        advancementsToGrant.put(id.get(), advancementProgress);
        ClientboundUpdateAdvancementsPacket packet = new ClientboundUpdateAdvancementsPacket(false, new ArrayList<>() {{
            add(new AdvancementHolder(id.get(), advancement));
        }}, new HashSet<>(), advancementsToGrant);
        serverPlayer.connection.send(packet);
        ClientboundUpdateAdvancementsPacket packet2 = new ClientboundUpdateAdvancementsPacket(false, new ArrayList<>(), new HashSet<>() {{
            add(id.get());
        }}, new HashMap<>());
        serverPlayer.connection.send(packet2);
    }
}