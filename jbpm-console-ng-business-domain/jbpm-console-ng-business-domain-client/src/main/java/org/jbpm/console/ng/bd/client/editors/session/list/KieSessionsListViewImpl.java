/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.console.ng.bd.client.editors.session.list;

import com.github.gwtbootstrap.client.ui.Button;

import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.cell.client.ActionCell;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import org.uberfire.client.common.BusyPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;
import org.uberfire.security.Identity;

import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.jbpm.console.ng.bd.client.i18n.Constants;
import org.jbpm.console.ng.bd.client.resources.BusinessDomainImages;
import org.jbpm.console.ng.bd.client.util.ResizableHeader;
import org.jbpm.console.ng.bd.model.KModuleDeploymentUnitSummary;
import org.jbpm.console.ng.bd.model.events.KieSessionSelectionEvent;

@Dependent
@Templated(value = "KieSessionsListViewImpl.html")
public class KieSessionsListViewImpl extends Composite implements KieSessionsListPresenter.KieSessionsListView {

    @Inject
    private Identity identity;
    @Inject
    private PlaceManager placeManager;
    private KieSessionsListPresenter presenter;
    
    @Inject
    @DataField
    public Label deploymentIDLabel;
    
    @Inject
    @DataField
    public TextBox deploymentIDText;
    
    @Inject
    @DataField
    public TextBox groupText;

    @Inject
    @DataField
    public TextBox artifactText;

    @Inject
    @DataField
    public TextBox versionText;

    @Inject
    @DataField
    public TextBox kbaseNameText;

    @Inject
    @DataField
    public TextBox kieSessionNameText;

    @Inject
    @DataField
    public Button deployUnitButton;

    @Inject
    @DataField
    public Label deployedUnitsLabel;
    
    @Inject
    @DataField
    public Label groupLabel;
    
    @Inject
    @DataField
    public Label artifactLabel;
    
    @Inject
    @DataField
    public Label versionLabel;
    
    @Inject
    @DataField
    public Label kbaseNameLabel;
    
    @Inject
    @DataField
    public Label kieSessionNameLabel;
    
    @Inject
    @DataField
    public DataGrid<KModuleDeploymentUnitSummary> deployedUnitsListGrid;

    @Inject
    @DataField
    public FlowPanel listContainerDeployedUnits;

    @Inject
    @DataField
    public SimplePager pager;
    
    @Inject
    @DataField
    public IconAnchor refreshIcon;

    private Set<KModuleDeploymentUnitSummary> selectedKieSession;
    @Inject
    private Event<NotificationEvent> notification;
    @Inject
    private Event<KieSessionSelectionEvent> kieSessionSelection;
    private ListHandler<KModuleDeploymentUnitSummary> sortHandler;

    private Constants constants = GWT.create(Constants.class);
    private BusinessDomainImages images = GWT.create(BusinessDomainImages.class);

    @Override
    public void init(final KieSessionsListPresenter presenter) {
        this.presenter = presenter;

        listContainerDeployedUnits.add(deployedUnitsListGrid);
        listContainerDeployedUnits.add(pager);
        
        deployUnitButton.setText(constants.Deploy_Unit());
        deployedUnitsLabel.setText(constants.Deploy_A_New_Unit());
        groupLabel.setText(constants.GroupID());
        artifactLabel.setText(constants.Artifact());
        versionLabel.setText(constants.Version());
        kbaseNameLabel.setText(constants.KieBaseName());
        kieSessionNameLabel.setText(constants.KieSessionName());
        deploymentIDLabel.setText(constants.Deploy_Unit());
        
        refreshIcon.setTitle(constants.Refresh());
        refreshIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.refreshDeployedUnits();
                displayNotification(constants.Deployed_Units_Refreshed());
            }
        });
        
        deployedUnitsListGrid.setHeight("350px");
        // Set the message to display when the table is empty.
        Label emptyTable = new Label(constants.No_Deployment_Units_Available());
        emptyTable.setStyleName("");
        deployedUnitsListGrid.setEmptyTableWidget(emptyTable);

        // Attach a column sort handler to the ListDataProvider to sort the list.
        sortHandler = new ListHandler<KModuleDeploymentUnitSummary>(presenter.getDataProvider().getList());
        deployedUnitsListGrid.addColumnSortHandler(sortHandler);

        // Create a Pager to control the table.

        pager.setDisplay(deployedUnitsListGrid);
        pager.setPageSize(10);

        // Add a selection model so we can select cells.
        final MultiSelectionModel<KModuleDeploymentUnitSummary> selectionModel = new MultiSelectionModel<KModuleDeploymentUnitSummary>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                selectedKieSession = selectionModel.getSelectedSet();
                for (KModuleDeploymentUnitSummary unit : selectedKieSession) {
                    //
                }
            }
        });

        deployedUnitsListGrid.setSelectionModel(selectionModel, DefaultSelectionEventManager.<KModuleDeploymentUnitSummary> createCheckboxManager());

        initTableColumns(selectionModel);

        presenter.addDataDisplay(deployedUnitsListGrid);

    }

    @EventHandler("deployUnitButton")
    public void deployUnitButton(ClickEvent e) {
        presenter.deployUnit(deploymentIDText.getText(), groupText.getText(), artifactText.getText(), versionText.getText(),
                kbaseNameText.getText(), kieSessionNameText.getText());
    }

    private void initTableColumns(final SelectionModel<KModuleDeploymentUnitSummary> selectionModel) {
        

        // Unit Id
        Column<KModuleDeploymentUnitSummary, String> unitIdColumn = new Column<KModuleDeploymentUnitSummary, String>(new TextCell()) {
            @Override
            public String getValue(KModuleDeploymentUnitSummary unit) {
                return unit.getId();
            }
        };
        unitIdColumn.setSortable(true);
        sortHandler.setComparator(unitIdColumn, new Comparator<KModuleDeploymentUnitSummary>() {
            @Override
            public int compare(KModuleDeploymentUnitSummary o1, KModuleDeploymentUnitSummary o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        deployedUnitsListGrid.addColumn(unitIdColumn, new ResizableHeader(constants.Unit(), deployedUnitsListGrid, unitIdColumn));
        
        
        // Unit Group Id
        Column<KModuleDeploymentUnitSummary, String> groupIdColumn = new Column<KModuleDeploymentUnitSummary, String>(new TextCell()) {
            @Override
            public String getValue(KModuleDeploymentUnitSummary unit) {
                return unit.getGroupId();
            }
        };
        groupIdColumn.setSortable(true);
        sortHandler.setComparator(groupIdColumn, new Comparator<KModuleDeploymentUnitSummary>() {
            @Override
            public int compare(KModuleDeploymentUnitSummary o1, KModuleDeploymentUnitSummary o2) {
                return o1.getGroupId().compareTo(o2.getGroupId());
            }
        });
        deployedUnitsListGrid.addColumn(groupIdColumn, new ResizableHeader(constants.GroupID(), deployedUnitsListGrid, groupIdColumn));
        
        // Unit Artifact Id
        Column<KModuleDeploymentUnitSummary, String> artifactIdColumn = new Column<KModuleDeploymentUnitSummary, String>(new TextCell()) {
            @Override
            public String getValue(KModuleDeploymentUnitSummary unit) {
                return unit.getArtifactId();
            }
        };
        artifactIdColumn.setSortable(true);
        sortHandler.setComparator(artifactIdColumn, new Comparator<KModuleDeploymentUnitSummary>() {
            @Override
            public int compare(KModuleDeploymentUnitSummary o1, KModuleDeploymentUnitSummary o2) {
                return o1.getArtifactId().compareTo(o2.getArtifactId());
            }
        });
        deployedUnitsListGrid.addColumn(artifactIdColumn, new ResizableHeader(constants.Artifact(), deployedUnitsListGrid, artifactIdColumn));
        
        
         // Unit Version
        Column<KModuleDeploymentUnitSummary, String> versionColumn = new Column<KModuleDeploymentUnitSummary, String>(new TextCell()) {
            @Override
            public String getValue(KModuleDeploymentUnitSummary unit) {
                return unit.getVersion();
            }
        };
        versionColumn.setSortable(true);
        sortHandler.setComparator(versionColumn, new Comparator<KModuleDeploymentUnitSummary>() {
            @Override
            public int compare(KModuleDeploymentUnitSummary o1, KModuleDeploymentUnitSummary o2) {
                return o1.getVersion().compareTo(o2.getVersion());
            }
        });
        deployedUnitsListGrid.addColumn(versionColumn, new ResizableHeader(constants.Version(), deployedUnitsListGrid, versionColumn));
        
         // Unit KBase
        Column<KModuleDeploymentUnitSummary, String> kbaseColumn = new Column<KModuleDeploymentUnitSummary, String>(new TextCell()) {
            @Override
            public String getValue(KModuleDeploymentUnitSummary unit) {
                return unit.getKbaseName();
            }
        };
        kbaseColumn.setSortable(true);
        sortHandler.setComparator(kbaseColumn, new Comparator<KModuleDeploymentUnitSummary>() {
            @Override
            public int compare(KModuleDeploymentUnitSummary o1, KModuleDeploymentUnitSummary o2) {
                return o1.getKbaseName().compareTo(o2.getKbaseName());
            }
        });
        deployedUnitsListGrid.addColumn(kbaseColumn, new ResizableHeader(constants.KieBaseName(), deployedUnitsListGrid, kbaseColumn));
        
         // Unit KBase
        Column<KModuleDeploymentUnitSummary, String> ksessionColumn = new Column<KModuleDeploymentUnitSummary, String>(new TextCell()) {
            @Override
            public String getValue(KModuleDeploymentUnitSummary unit) {
                return unit.getKsessionName();
            }
        };
        ksessionColumn.setSortable(true);
        sortHandler.setComparator(ksessionColumn, new Comparator<KModuleDeploymentUnitSummary>() {
            @Override
            public int compare(KModuleDeploymentUnitSummary o1, KModuleDeploymentUnitSummary o2) {
                return o1.getKsessionName().compareTo(o2.getKsessionName());
            }
        });
        deployedUnitsListGrid.addColumn(ksessionColumn, new ResizableHeader(constants.KieSessionName(), deployedUnitsListGrid, ksessionColumn));
        
        

        // actions (icons)
        List<HasCell<KModuleDeploymentUnitSummary, ?>> cells = new LinkedList<HasCell<KModuleDeploymentUnitSummary, ?>>();

        cells.add(new DeleteActionHasCell("Undeploy", new Delegate<KModuleDeploymentUnitSummary>() {
            @Override
            public void execute(KModuleDeploymentUnitSummary unit) {
                
                presenter.undeployUnit(unit.getId(), unit.getGroupId(), unit.getArtifactId(), 
                                            unit.getVersion(), unit.getKbaseName(), unit.getKsessionName());

            }
        }));

        cells.add(new DetailsActionHasCell("Details", new Delegate<KModuleDeploymentUnitSummary>() {
            @Override
            public void execute(KModuleDeploymentUnitSummary unit) {

               
                displayNotification("Deployment Unit " + unit.getId() + " go to details here!!");
            }
        }));

        CompositeCell<KModuleDeploymentUnitSummary> cell = new CompositeCell<KModuleDeploymentUnitSummary>(cells);
        Column<KModuleDeploymentUnitSummary, KModuleDeploymentUnitSummary> actionsColumn 
                        = new Column<KModuleDeploymentUnitSummary, KModuleDeploymentUnitSummary>(cell) {
            @Override
            public KModuleDeploymentUnitSummary getValue(KModuleDeploymentUnitSummary object) {
                return object;
            }
        };
        deployedUnitsListGrid.addColumn(actionsColumn, constants.Actions());
        deployedUnitsListGrid.setColumnWidth(actionsColumn, "70px");
    }

    @Override
    public void displayNotification(String text) {
        notification.fire(new NotificationEvent(text));
    }

    @Override
    public void showBusyIndicator(final String message) {
        BusyPopup.showMessage(message);
    }

    @Override
    public void hideBusyIndicator() {
        BusyPopup.close();
    }

    @Override
    public void cleanForm() {
        this.artifactText.setText("");
        this.groupText.setText("");
        this.versionText.setText("");
        this.kbaseNameText.setText("");
        this.kieSessionNameText.setText("");
        this.deploymentIDText.setText("");
    }

    public DataGrid<KModuleDeploymentUnitSummary> getDataGrid() {
        return deployedUnitsListGrid;
    }

    public ListHandler<KModuleDeploymentUnitSummary> getSortHandler() {
        return sortHandler;
    }

    private class DeleteActionHasCell implements HasCell<KModuleDeploymentUnitSummary, KModuleDeploymentUnitSummary> {

        private ActionCell<KModuleDeploymentUnitSummary> cell;

        public DeleteActionHasCell(String text, Delegate<KModuleDeploymentUnitSummary> delegate) {
            cell = new ActionCell<KModuleDeploymentUnitSummary>(text, delegate) {
                @Override
                public void render(Cell.Context context, KModuleDeploymentUnitSummary value, SafeHtmlBuilder sb) {

                    AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.undeployGridIcon());
                    SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                    mysb.appendHtmlConstant("<span title='"+constants.Undeploy()+"'>");
                    mysb.append(imageProto.getSafeHtml());
                    mysb.appendHtmlConstant("</span>");
                    sb.append(mysb.toSafeHtml());
                }
            };
        }

        @Override
        public Cell<KModuleDeploymentUnitSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<KModuleDeploymentUnitSummary, KModuleDeploymentUnitSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public KModuleDeploymentUnitSummary getValue(KModuleDeploymentUnitSummary object) {
            return object;
        }
    }

    private class DetailsActionHasCell implements HasCell<KModuleDeploymentUnitSummary, KModuleDeploymentUnitSummary> {

        private ActionCell<KModuleDeploymentUnitSummary> cell;

        public DetailsActionHasCell(String text, Delegate<KModuleDeploymentUnitSummary> delegate) {
            cell = new ActionCell<KModuleDeploymentUnitSummary>(text, delegate) {
                @Override
                public void render(Cell.Context context, KModuleDeploymentUnitSummary value, SafeHtmlBuilder sb) {

                    AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.detailsGridIcon());
                    SafeHtmlBuilder mysb = new SafeHtmlBuilder();
                    mysb.appendHtmlConstant("<span title='"+constants.Details()+"'>");
                    mysb.append(imageProto.getSafeHtml());
                    mysb.appendHtmlConstant("</span>");
                    sb.append(mysb.toSafeHtml());
                }
            };
        }

        @Override
        public Cell<KModuleDeploymentUnitSummary> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<KModuleDeploymentUnitSummary, KModuleDeploymentUnitSummary> getFieldUpdater() {
            return null;
        }

        @Override
        public KModuleDeploymentUnitSummary getValue(KModuleDeploymentUnitSummary object) {
            return object;
        }
    }

}
