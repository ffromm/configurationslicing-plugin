package configurationslicing.cleanup;

import configurationslicing.BooleanSlicer;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.plugins.ws_cleanup.WsCleanup;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.io.IOException;
import java.util.List;

@Extension
public class WorkspaceCleanupBoolSlicer extends BooleanSlicer<AbstractProject<?,?>> {
    public WorkspaceCleanupBoolSlicer() {
        super(new WorkspaceCleanUpSpec());
    }
    public static class WorkspaceCleanUpSpec implements BooleanSlicerSpec<AbstractProject<?,?>>
    {
        public String getName() {
            return "Cleanup workspace after build (bool)";
        }

        public String getName(AbstractProject<?,?> item) {
            return item.getName();
        }

        public String getUrl() {
            return "cleanupworkspacebool";
        }

        public boolean getValue(AbstractProject<?,?> item) {
            DescribableList<Publisher,Descriptor<Publisher>> publishersList = item.getPublishersList();

            boolean wsCleanupAfterBuild = false;

            for (Publisher publisher : publishersList) {
                if(publisher instanceof WsCleanup) {
                    wsCleanupAfterBuild = true;
                }
            }
            return wsCleanupAfterBuild;
        }

        public List<AbstractProject<?,?>> getWorkDomain() {
            return (List)Hudson.getInstance().getItems(AbstractProject.class);
        }

        public boolean setValue(AbstractProject<?,?> item, boolean value) {
            DescribableList<Publisher,Descriptor<Publisher>> publishersList = item.getPublishersList();

            boolean hasWsCleanup = false;

            for (Publisher publisher : publishersList) {
                if(publisher instanceof WsCleanup) {
                    hasWsCleanup = true;
                    break;
                }
            }

            if(!value && hasWsCleanup) {
                try {
                    publishersList.remove(WsCleanup.class);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }

            if(value && !hasWsCleanup) {
                try {
                    WsCleanup wsCleanup = new WsCleanup(null, false);
                    publishersList.add(wsCleanup);

                    return true;
                } catch (IOException e) {
                    return false;
                }
            }

            return value != hasWsCleanup;
        }
    }
}
