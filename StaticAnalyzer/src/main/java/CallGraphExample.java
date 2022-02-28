import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class CallGraphExample {

    /**
     * @param projectJar JAR of the project to be analyzed
     * @return the {@link AnalysisScope} object indicating what classes are available in the class path
     * @throws URISyntaxException
     */
    public static AnalysisScope createScope(String projectJar) throws URISyntaxException, IOException {
        URL jreUrl = CallGraphExample.class.getResource("jdk-17.0.1/rt.jar");
        File exFile = new FileProvider().getFile("Java60RegressionExclusions.txt");

        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(projectJar, exFile);
        AnalysisScopeReader.addClassPathToScope(jreUrl.getPath(), scope, ClassLoaderReference.Primordial);
        return scope;
    }

    public CallGraph buildChaCallGraph(AnalysisScope scope, IClassHierarchy classHierarchy) throws CancelException {
        CHACallGraph cg = new CHACallGraph(classHierarchy, true);
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, classHierarchy);
        cg.init(entrypoints);
        return cg;
    }

    public static CallGraph buildRtaCallGraph(AnalysisScope scope, IClassHierarchy classHierarchy) throws CallGraphBuilderCancelException {
        AnalysisOptions options = new AnalysisOptions();
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, classHierarchy);
        options.setEntrypoints(entrypoints);
        AnalysisCache analysisCache = new AnalysisCacheImpl();
        return Util.makeRTABuilder(options, analysisCache, classHierarchy, scope).makeCallGraph(options, null);
    }

    public static CallGraph buildNCfaCallGraph(AnalysisScope scope, IClassHierarchy classHierarchy, int n) throws CallGraphBuilderCancelException {
        AnalysisOptions options = new AnalysisOptions();
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, classHierarchy);
        options.setEntrypoints(entrypoints);
        AnalysisCache analysisCache = new AnalysisCacheImpl();
        return Util.makeNCFABuilder(n, options, analysisCache, classHierarchy, scope).makeCallGraph(options, null);
    }


    public static void main(String[] args) throws IOException, WalaException, URISyntaxException, CancelException {
        // creates an analysis scope
        AnalysisScope scope = createScope(CallGraphExample.class.getResource("Example1.jar").getPath());
        // build the class hierarchy
        IClassHierarchy cha = ClassHierarchyFactory.make(scope);

        CallGraph cg = buildNCfaCallGraph(scope, cha, 1);

    }
}


