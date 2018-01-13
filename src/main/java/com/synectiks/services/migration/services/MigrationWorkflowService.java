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
import org.springframework.http.MediaType;
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
		if (!IUtils.isNull(eventData) && eventData.length() > 0) {
			IStateUtils.addExtendedVariable(rest, getStateUrl(), workflowId,
					IConsts.OAK_NODE, eventData);
		}
		Map<String, OakFileNode> lstFiles = null;
		if (!IUtils.isNull(files) && files.length > 0) {
			lstFiles = IUtils.getFileNodes(files, tmpPath, null);
			IStateUtils.addExtendedVariable(rest, getStateUrl(), workflowId,
					IConsts.OAK_FILE_NODE, lstFiles);
		}
		createOakNode(request, eventData, workflowId, event, lstFiles);
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

	private void createOakNode(HttpServletRequest request, JSONObject oakNode,
			String workflowId, String event, Map<?, ?> nodes) throws Exception {
		String url = env.getProperty(IConsts.OAK_URL_KEY, "");
		logger.info("Saving oak node " + oakNode);
		if (!IUtils.isNull(oakNode)) {
			String nodePath = getNodePath(request, workflowId);
			url += "/createNode";
			try {
				String res = IUtils.sendPostRestRequest(rest, url, null,
						String.class, IUtils.getParamMap(nodePath, oakNode, event),
						MediaType.APPLICATION_FORM_URLENCODED);
				logger.info("Result: " + res);
				saveFileNodes(url, nodePath + "/" + event, nodes);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			throw new Exception("No node data to store");
		}
	}

	private String getStateUrl() {
		if (IUtils.isNullOrEmpty(stateUrl)) {
			stateUrl = env.getProperty(IConsts.STATE_URL_KEY, "");
		}
		return stateUrl;
	}

	/**
	 * Method to crate jcr node path
	 * @param request 
	 * @param workflowId 
	 * @return
	 */
	static String getNodePath(HttpServletRequest request, String workflowId) {
		StringBuilder sb = new StringBuilder();
			sb.append(IConsts.OAK_ROOT);
			String val = IUtils.getUserFromRequest(request);
			sb.append(IUtils.isNullOrEmpty(val) ? "" : "/" + val);
			val = IUtils.extractSubscriptionId(workflowId);
			sb.append(IUtils.isNullOrEmpty(val) ? "" : "/" + val);
		return sb.toString();
	}

	/**
	 * Method to save oak file nodes into jcr repository
	 * @param url
	 * @param parentPath
	 * @param nodes
	 */
	private void saveFileNodes(String url, String parentPath, Map<?, ?> nodes) {
		nodes.entrySet().forEach(entry -> {
			logger.info(entry.getKey() + " => [" + entry.getValue().getClass().getName()
					+ "]");
			OakFileNode node = null;
			if (!IUtils.isNull(entry.getValue()) && entry.getValue() instanceof String) {
				node = IUtils.getObjectFromValue(entry.getValue().toString(),
						OakFileNode.class);
			}
			String res = IUtils.sendPostRestRequest(rest, url, null, String.class,
					IUtils.getParamMap(parentPath, node, entry.getKey().toString()),
					MediaType.APPLICATION_FORM_URLENCODED);
			logger.info("Result: " + res);
		});
	}

}
