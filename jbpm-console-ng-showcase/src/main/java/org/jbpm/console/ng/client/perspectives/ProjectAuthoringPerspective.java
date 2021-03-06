/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.client.perspectives;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.New;
import javax.inject.Inject;
import org.kie.guvnor.commons.ui.client.handlers.NewResourcesMenu;
import org.kie.guvnor.commons.ui.client.menu.ResourceMenuBuilder;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.mvp.Command;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.Position;
import org.uberfire.client.workbench.model.PanelDefinition;
import org.uberfire.client.workbench.model.PerspectiveDefinition;
import org.uberfire.client.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.client.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.client.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.client.workbench.widgets.menu.MenuBar;
import org.uberfire.client.workbench.widgets.menu.impl.DefaultMenuBar;
import org.uberfire.client.workbench.widgets.menu.impl.DefaultMenuItemCommand;
import org.uberfire.shared.mvp.impl.DefaultPlaceRequest;

/**
 *
 * @author salaboy
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "Authoring")
public class ProjectAuthoringPerspective {

  private ResourceMenuBuilder menuBuilder;
  @Inject
  private PlaceManager placeManager;
  private PerspectiveDefinition perspective;
  private MenuBar menuBar;
  
  @Inject
  private NewResourcesMenu newResourcesMenu;

  public ProjectAuthoringPerspective() {
  }

  @PostConstruct
  public void init() {

    buildMenuBar();

  }

  @Inject
  public ProjectAuthoringPerspective(@New ResourceMenuBuilder menuBuilder) {
    this.menuBuilder = menuBuilder;
  }

  @Perspective
  public PerspectiveDefinition getPerspective() {
    final PerspectiveDefinition p = new PerspectiveDefinitionImpl();
    p.setName("Project Authoring Perspective");

    final PanelDefinition west = new PanelDefinitionImpl();
    west.setWidth(300);
    west.setMinWidth(200);
    west.addPart(new PartDefinitionImpl(new DefaultPlaceRequest("org.kie.guvnor.explorer")));
    p.getRoot().insertChild(Position.WEST, west);
    p.setTransient(true);
    return p;
  }

  @WorkbenchMenu
  public MenuBar getMenuBar() {
    return this.menuBar;
  }

  private void buildMenuBar() {
    this.menuBar = new DefaultMenuBar();
    menuBar.addItem(new DefaultMenuItemCommand("Projects",
            new Command() {
      @Override
      public void execute() {
        placeManager.goTo("org.kie.guvnor.explorer");
      }
    }));
    
    this.menuBar.addItem(newResourcesMenu);
  }
}
