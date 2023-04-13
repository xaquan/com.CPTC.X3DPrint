package com.CPTC.X3DPrint.impl;

import java.util.Locale;
import java.util.UUID;

import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class X3DPrintProgramNodeService implements SwingProgramNodeService<X3DPrintProgramNodeContribution, X3DPrintProgramNodeView> {

	@Override
	public String getId() {
		return "3DPrint"+UUID.randomUUID();
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
//		configuration.setChildrenAllowed(true);
		
	}

	@Override
	public String getTitle(Locale locale) {		
		return "3D_Print";
	}

	@Override
	public X3DPrintProgramNodeView createView(ViewAPIProvider apiProvider) {
		return new X3DPrintProgramNodeView(apiProvider);
	}

	@Override
	public X3DPrintProgramNodeContribution createNode(ProgramAPIProvider apiProvider, X3DPrintProgramNodeView view,
			DataModel model, CreationContext context) {
		return new X3DPrintProgramNodeContribution(apiProvider, view, model);
	}

}
