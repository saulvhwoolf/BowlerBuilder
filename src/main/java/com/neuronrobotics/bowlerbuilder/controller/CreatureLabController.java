package com.neuronrobotics.bowlerbuilder.controller;

import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.view.JogView;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;

public class CreatureLabController {

  private static final Logger logger =
      LoggerUtilities.getLogger(CreatureLabController.class.getSimpleName());

  @FXML
  private ProgressIndicator cadProgress;
  @FXML
  private CheckBox autoRegenCAD;
  @FXML
  private TabPane creatureTabPane;
  @FXML
  private Tab blackTab;
  @FXML
  private Tab blueTab;
  @FXML
  private BorderPane contentPane;

  private WeakHashMap<TreeItem<String>, Runnable> treeViewOnActions;

  @Inject
  public CreatureLabController() {
    treeViewOnActions = new WeakHashMap<>();
  }

  @FXML
  protected void initialize() {
    blackTab.setGraphic(AssetFactory.loadIcon("creature.png"));
    blueTab.setGraphic(AssetFactory.loadIcon("Move-Limb.png"));
  }

  public void generateMenus(MobileBase device) {
    logger.log(Level.INFO, "Got RobotLoaded event on: " + Thread.currentThread().getName());

    //    FxUtil.runFX(() -> {
    //      TreeItem<String> root = new TreeItem<>(device.getScriptingName(),
    //          AssetFactory.loadIcon("creature.png"));
    //      treeView.setRoot(root);
    //
    //      root.getChildren().add(loadLimbs(new TreeItem<>("Legs",
    //          AssetFactory.loadIcon("Load-Limb-Legs.png")), device.getLegs()));
    //      root.getChildren().add(loadLimbs(new TreeItem<>("Arms",
    //          AssetFactory.loadIcon("Load-Limb-Arms.png")), device.getAppendages()));
    //      root.getChildren().add(loadLimbs(new TreeItem<>("Steerable Wheels",
    //          AssetFactory.loadIcon("Load-Limb-Steerable-Wheels.png")), device.getSteerable()));
    //      root.getChildren().add(loadLimbs(new TreeItem<>("Fixed Wheels",
    //          AssetFactory.loadIcon("Load-Limb-Fixed-Wheels.png")), device.getDrivable()));
    //
    //      treeView.getSelectionModel().selectedItemProperty()
    //          .addListener((observable, oldValue, newValue) -> {
    //            Runnable runnable = treeViewOnActions.get(newValue);
    //            if (runnable != null) {
    //              runnable.run();
    //            }
    //          });
    //    });
  }

  private TreeItem<String> loadLimbs(TreeItem<String> root, List<DHParameterKinematics> drivable) {
    drivable.forEach(limb -> root.getChildren().add(loadSingleLimb(limb)));
    return root;
  }

  private TreeItem<String> loadSingleLimb(DHParameterKinematics dh) {
    TreeItem<String> dhItem = new TreeItem<>(dh.getScriptingName(),
        AssetFactory.loadIcon("Move-Limb.png"));
    treeViewOnActions.put(dhItem, () -> contentPane.setCenter(new JogView()));

    int i = 0;
    for (LinkConfiguration conf : dh.getFactory().getLinkConfigurations()) {
      dhItem.getChildren().add(loadSingleLink(conf, dh, i++));
    }

    TreeItem<String> placeLimb = new TreeItem<>("Move Root Of Limb",
        AssetFactory.loadIcon("Design-Parameter-Adjustment.png"));
    dhItem.getChildren().add(placeLimb);

    TreeItem<String> addLink = new TreeItem<>("Add Link",
        AssetFactory.loadIcon("Add-Link.png"));
    dhItem.getChildren().add(addLink);

    TreeItem<String> advanced = new TreeItem<>("Advanced Configuration",
        AssetFactory.loadIcon("Advanced-Configuration.png"));
    dhItem.getChildren().add(advanced);

    TreeItem<String> remove = new TreeItem<>("Remove " + dh.getScriptingName(),
        AssetFactory.loadIcon("Remove-Limb.png"));
    dhItem.getChildren().add(remove);

    return dhItem;
  }

  private TreeItem<String> loadSingleLink(LinkConfiguration conf,
                                          DHParameterKinematics dh, Integer index) {
    TreeItem<String> link = new TreeItem<>(conf.getName(),
        AssetFactory.loadIcon("Move-Single-Motor.png"));

    DHLink dhLink = dh.getChain().getLinks().get(index);
    //    LinkSliderWidget linkSliderWidget = new LinkSliderWidget(index, dhLink, device);

    TreeItem<String> design = new TreeItem<>("Design Parameters " + conf.getName(),
        AssetFactory.loadIcon("Design-Parameter-Adjustment.png"));
    link.getChildren().add(design);

    TreeItem<String> hwConfig = new TreeItem<>("Hardware Config " + conf.getName(),
        AssetFactory.loadIcon("Hardware-Config.png"));
    link.getChildren().add(hwConfig);

    TreeItem<String> slaves = new TreeItem<>("Slaves to " + conf.getName(),
        AssetFactory.loadIcon("Slave-Links.png"));
    TreeItem<String> addSlaves = new TreeItem<>("Add Slave to " + conf.getName(),
        AssetFactory.loadIcon("Add-Slave-Links.png"));
    slaves.getChildren().add(addSlaves);
    link.getChildren().add(slaves);

    TreeItem<String> remove = new TreeItem<>("Remove " + conf.getName(),
        AssetFactory.loadIcon("Remove-Link.png"));
    link.getChildren().add(remove);

    return link;
  }

  public ProgressIndicator getCadProgress() {
    return cadProgress;
  }

}
