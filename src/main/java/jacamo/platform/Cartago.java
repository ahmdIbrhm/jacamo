package jacamo.platform;

import java.util.logging.Level;
import java.util.logging.Logger;

import cartago.AgentIdCredential;
import cartago.ArtifactId;
import cartago.CartagoContext;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.WorkspaceId;
import jaca.CartagoEnvironment;
import jacamo.infra.EnvironmentInspector;
import jacamo.infra.EnvironmentInspectorWeb;
import jacamo.project.JaCaMoWorkspaceParameters;
import jason.mas2j.ClassParameters;

public class Cartago extends DefaultPlatform {
    
    protected CartagoEnvironment  env;
    protected CartagoContext      cartagoCtx;

    //protected Map<String, ArtifactId> artIds;

    Logger logger = Logger.getLogger(Cartago.class.getName());

    @Override
    public void init(String[] args) {
        env = new CartagoEnvironment();
        env.init( args );
    }
    
    /*public void setArtIdsMap(Map<String, ArtifactId> artIds) {
        this.artIds = artIds;
    }*/
    
    @Override
    public void start() {
        try {
            cartagoCtx = CartagoService.startSession(CartagoService.MAIN_WSP_NAME, new AgentIdCredential("JaCaMo_Launcher"));
        } catch (CartagoException e1) {
            e1.printStackTrace();
            return;
        }
        for (JaCaMoWorkspaceParameters wp: project.getWorkspaces()) {
            try {
                if (project.isInDeployment(wp.getNode())) {
                    if (project.getNodeHost(wp.getNode()) != null) {
                        logger.warning("**** Remote workspace creation is not implemented yet! The workspace @ "+project.getNodeHost(wp.getNode())+" wasn't created");
                        continue;
                    }
                    CartagoService.createWorkspace(wp.getName());
                    logger.info("Workspace "+wp.getName()+" created.");
                    EnvironmentInspectorWeb.registerWorkspace(wp.getName());

                    cartagoCtx.joinWorkspace(wp.getName(), new AgentIdCredential("JaCaMoLauncherAg"));
                    WorkspaceId wid = cartagoCtx.getJoinedWspId(wp.getName());

                    for (String aName: wp.getArtifacts().keySet()) {
                        String m = null;
                        try {
                            ClassParameters cp = wp.getArtifacts().get(aName);
                            m = "artifact "+aName+": "+cp.getClassName()+"("+cp.getParametersStr(",")+") at "+wp.getName();
                            ArtifactId aid = cartagoCtx.makeArtifact(wid, aName, cp.getClassName(), cp.getTypedParametersArray());
                            //artIds.put(aName, aid);
                            logger.info(m+" created.");
                            if (wp.hasDebug())
                                EnvironmentInspector.addInGui(wp.getName(), aid);
                        } catch (CartagoException e) {
                            logger.log(Level.SEVERE, "error creating "+m,e);
                        }
                    }
                    if (wp.hasDebug()) {
                        CartagoService.enableDebug(wp.getName());
                    }
                }
            } catch (CartagoException e) {
                logger.log(Level.SEVERE, "error creating environmet, workspace:"+wp.getName(),e);
            }
        }
    }
    
    @Override
    public void stop() {
        try {
            CartagoService.shutdownNode();
        } catch (CartagoException e) {
            e.printStackTrace();
        }
    }
}
