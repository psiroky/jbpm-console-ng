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
package org.jbpm.console.ng.pr.client.editors.definition.list;



import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.TextBox;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;


import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import javax.enterprise.event.Event;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.jbpm.console.ng.bd.service.KnowledgeDomainServiceEntryPoint;
import org.jbpm.console.ng.pr.model.events.ProcessInstanceCreated;
import org.jbpm.console.ng.bd.service.StatefulKnowledgeSessionEntryPoint;
import org.jbpm.console.ng.pr.model.ProcessSummary;
import org.uberfire.client.annotations.OnReveal;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;

@Dependent
@WorkbenchScreen(identifier = "Process Definition List")
public class ProcessDefinitionListPresenter {

    public interface InboxView
            extends
            UberView<ProcessDefinitionListPresenter> {

        void displayNotification(String text);

        TextBox getSessionIdText();

        DataGrid<ProcessSummary> getDataGrid();
    }
    @Inject
    private InboxView view;
    @Inject
    private Caller<KnowledgeDomainServiceEntryPoint> knowledgeServices;
    @Inject
    Caller<StatefulKnowledgeSessionEntryPoint> ksessionServices;
    @Inject
    Event<ProcessInstanceCreated> processInstanceCreatedEvents;
    private ListDataProvider<ProcessSummary> dataProvider = new ListDataProvider<ProcessSummary>();

    @WorkbenchPartTitle
    public String getTitle() {
        return "Process Definition List";
    }

    @WorkbenchPartView
    public UberView<ProcessDefinitionListPresenter> getView() {
        return view;
    }

    public ProcessDefinitionListPresenter() {
    }

    @PostConstruct
    public void init() {
    }

    public void fetchProcessDefs() {
        knowledgeServices.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void nothing) {
                view.displayNotification(" Repository Updated! ");
                knowledgeServices.call(new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void nothing) {
                        view.displayNotification(" KSession recreated! ");
                        refreshProcessList(view.getSessionIdText().getText());

                    }
                }).createDomain();
            }
        }).fetchChanges();
    }

    public void refreshProcessList(final String filter) {

        if (filter != null && !filter.equals("")) {
            knowledgeServices.call(new RemoteCallback<List<ProcessSummary>>() {
                @Override
                public void callback(List<ProcessSummary> processes) {
                    dataProvider.getList().clear();
                    dataProvider.getList().addAll(processes);
                    dataProvider.refresh();
                }
            }).getProcessesByFilter(filter);
        } else {
            knowledgeServices.call(new RemoteCallback<List<ProcessSummary>>() {
                @Override
                public void callback(List<ProcessSummary> processes) {
                    dataProvider.getList().clear();
                    dataProvider.getList().addAll(processes);
                    dataProvider.refresh();
                }
            }).getProcesses();
        }
    }

    public void startProcessInstance(final String processId) {

        ksessionServices.call(new RemoteCallback<Long>() {
            @Override
            public void callback(Long processId) {
                view.displayNotification("Process Created (id = " + processId + ")");
                processInstanceCreatedEvents.fire(new ProcessInstanceCreated());
            }
        }).startProcess(0 , processId);

    }

    public void addDataDisplay(HasData<ProcessSummary> display) {
        dataProvider.addDataDisplay(display);
    }

    public ListDataProvider<ProcessSummary> getDataProvider() {
        return dataProvider;
    }

    public void refreshData() {
        dataProvider.refresh();
    }

    @OnReveal
    public void onReveal() {
        refreshProcessList("");
    }
}
