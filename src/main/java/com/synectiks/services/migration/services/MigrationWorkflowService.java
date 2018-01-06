/**
 * 
 */
package com.synectiks.services.migration.services;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.synectiks.commons.constants.IConsts;
import com.synectiks.commons.entities.oak.OakFileNode;
import com.synectiks.commons.utils.IUtils;
import com.synectiks.services.migration.utils.IStateUtils;

/**
 * @author Rajesh
 */
@Service
public class MigrationWorkflowService {

	private static Logger logger = LoggerFactory
			.getLogger(MigrationWorkflowService.class);

	private static final String START_EVENT = "Start";

	@Autowired
	private Environment env;
	@Autowired
	private RestTemplate rest;
	private String stateUrl;

	/**
	 * Method create migration workflow state machine for given id.
	 * @param id
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public String createMigrationWorkflow(String id, HttpServletRequest request)
			throws Exception {
		String machineId = IStateUtils.getMachineId(rest, getStateUrl(), id);
		logger.info("workflowId: " + machineId);
		sendEvent(machineId, START_EVENT);
		logger.info("CurState: "
				+ IStateUtils.getCurrentState(rest, getStateUrl(), machineId));
		return machineId;
	}

	/**
	 * Service method to fire an event on state with required submit data to
	 * store
	 * @param request
	 * @param workflowId
	 * @param event
	 * @param eventData
	 * @param files
	 * @param tmpPath
	 * @return
	 * @throws Exception
	 */
	public boolean updateWorkflow(HttpServletRequest request, String workflowId,
			String event, JSONObject eventData, MultipartFile[] files, String tmpPath)
			throws Exception {
		IStateUtils.addExtendedVariable(rest, getStateUrl(), workflowId, IConsts.USERNAME,
				IUtils.getUserFromRequest(request));
		IStateUtils.addExtendedVariable(rest, getStateUrl(), workflowId,
				IConsts.SUBSCRIPTION_ID, IUtils.extractSubscriptionId(workflowId));
		String oakUrl = env.getProperty(IConsts.OAK_URL_KEY, "");
		IStateUtils.addExtendedVariable(rest, getStateUrl(), workflowId, IConsts.OAK_URL,
				oakUrl);
		IStateUtils.addExtendedVariable(rest, getStateUrl(), workflowId, IConsts.EVENT,
				event);
		if (!IUtils.isNull(eventData) && eventData.length() > 0) {
			IStateUtils.addExtendedVariable(rest, getStateUrl(), workflowId,
					IConsts.OAK_NODE, eventData);
		}
		if (!IUtils.isNull(files) && files.length > 0) {
			Map<String, OakFileNode> lstFiles = IUtils.getFileNodes(files, tmpPath, null);
			IStateUtils.addExtendedVariable(rest, getStateUrl(), workflowId,
					IConsts.OAK_FILE_NODE, lstFiles);
		}
		logger.info("CurState: "
				+ IStateUtils.getCurrentState(rest, getStateUrl(), workflowId));
		boolean res = sendEvent(workflowId, event);
		return res;
	}

	/**
	 * Method to send event with extended variable events.
	 * @param ssmId Spring state machine id
	 * @param event
	 * @return
	 * @throws Exception
	 */
	private boolean sendEvent(String ssmId, String event) throws Exception {
		logger.info("Send " + event);
		boolean res = IStateUtils.sendEvent(rest, getStateUrl(), ssmId, event);
		if (!res) {
			throw new Exception("Event " + event + " is not applicable on current state "
					+ IStateUtils.getCurrentState(rest, getStateUrl(), ssmId));
		}
		logger.info(
				"CurState: " + IStateUtils.getCurrentState(rest, getStateUrl(), ssmId));
		return res;
	}

	private String getStateUrl() {
		if (IUtils.isNullOrEmpty(stateUrl)) {
			stateUrl = env.getProperty(IConsts.STATE_URL_KEY, "");
		}
		return stateUrl;
	}

}
