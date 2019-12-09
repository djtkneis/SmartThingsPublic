 metadata {
 
	definition (name: "Aeon Multisensor 6 - AN 1.0", namespace: "aneis", author: "Anthony Neis") {
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Ultraviolet Index"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
		capability "Power Source"
		capability "Tamper Alert"

		attribute "batteryStatus", "string"

		// CCs supported - 94, 134, 114, 132, 89, 133, 115, 113, 128, 48, 49, 112, 152, 122
		fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0xEF,0x5A,0x98,0x7A"
    }
        
	simulator {
		status "no motion" : "command: 9881, payload: 00300300"
		status "motion"    : "command: 9881, payload: 003003FF"
        status "clear" : " command: 9881, payload: 0071050000000007030000"
        status "tamper" : "command: 9881, payload: 007105000000FF07030000"
        
        // Simulate Temperature report events
        for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                	scaledSensorValue: i,
                    precision: 1,
                    sensorType: 1,
                    scale: 1
				)
			).incomingMessage()
		}
        
        // Simulate RH report events
		for (int i = 0; i <= 100; i += 20) {
			status "RH ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                	scaledSensorValue: i,
                    sensorType: 5
            	)
			).incomingMessage()
		}
        
        // Simulate Illumination report events
		for (int i in [0, 1, 2, 8, 12, 16, 20, 24, 30, 64, 82, 100, 200, 500, 1000]) {
			status "illuminance ${i} lux": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i,
                sensorType: 3
                )
			).incomingMessage()
		}
        
        // Simulate UV report events
		for (int i = 0; i <= 11; i += 1) {
			status "ultravioletultravioletIndex ${i}": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i,
                sensorType: 27
                )
			).incomingMessage()
		}
        
        // Simulate Battery events
		for (int i in [0, 5, 10, 15, 50, 99, 100]) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
                batteryLevel: i
                )
			).incomingMessage()
		}
        // Simulate Low Battery alerts
		status "low battery alert": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
            	batteryLevel: 255
            	)
			).incomingMessage()
            
        // Simulate Wake Up
		status "wake up": "command: 8407, payload:"
	}
    
	tiles (scale: 2) {
        // Main Tile
		multiAttributeTile(name:"main", type:"generic", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
            	attributeState "temperature",label:'${currentValue}',backgroundColors:[
                	[value: 32, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
				]       
            }
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState "humidity",label:'RH ${currentValue}%',precision:2,icon:" "
            }
		}
        // Motion
        standardTile("motion","device.motion", width: 2, height: 2) {
            	state "active",label:'motion',icon:"st.motion.motion.active",backgroundColor:"#53a7c0"
                state "inactive",label:'no motion',icon:"st.motion.motion.inactive",backgroundColor:"#ffffff"
			}
		// Humidity
        valueTile("humidity","device.humidity", width: 2, height: 2) {
           	state "humidity",label:'RH ${currentValue}%', precision:2
			}
        // Illumination
        valueTile("illuminance","device.illuminance", width: 2, height: 2) {
            	state "luminosity",label:'${currentValue} lx', precision:2, backgroundColors:[
                	[value: 0, color: "#000000"],
                    [value: 1, color: "#060053"],
                    [value: 3, color: "#3e3900"],
                    [value: 12, color: "#8e8400"],
					[value: 24, color: "#c5c08B"],
					[value: 36, color: "#dad7b6"],
					[value: 128, color: "#f3f2e9"],
                    [value: 1000, color: "#ffffff"]
				]
			}
        // UV
		valueTile("ultravioletIndex","device.ultravioletIndex", width: 2, height: 2) {
				state "ultravioletIndex",label:'${currentValue} UV INDEX'
			}
        // Vibration
		valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
			state "clear", label: 'tamper clear', backgroundColor: "#ffffff"
			state "detected", label: 'tampered', backgroundColor: "#ff0000"
		}
        // Battery Level
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        // Send configuration message
		standardTile("configure","device.configure", decoration: "flat", width: 2, height: 2) {
			state "configure", label:'config', action:"configure", icon:"st.secondary.tools"
		}
        
		main(["main"])
		details(["main","humidity","illuminance","ultravioletIndex","motion","tamper","battery","configure"])
	}
	
    preferences {
		input "debugOutput", 
        	"boolean", 
			title: "Enable debug logging?",
            description: "Used for turning on/off debug logging",
			defaultValue: false,
			displayDuringSetup: true
		input "tempoffset",
			"number",
			title: "Temperature offset",
            description: "Offset to use for reporting temperature (-11 to 11)",
            range: "-11..11",
			defaultValue: 0,
            required: false,
            displayDuringSetup: false
		input "humidityoffset",
        	"number",
            title: "Humidity offset",
            description: "Offset to use for humidity reporting (-50 to 50)",
			range: "-50..50",
			defaultValue: 0,
			required: false,
            displayDuringSetup: false
		input "luminanceoffset",
          	"number",
            title: "Luminance offset",
            description: "Offset to use for lumination reporting (-1000 to 1000)",
            range: "-1000..1000",
			defaultValue: 0,
            required: false,
	        displayDuringSetup: false
		input "ultravioletoffset",
          	"number",
            title: "Ultraviolet offset",
            description: "Offset to use for UV reporting (-10 to 10)",
            range: "-10..10",
			defaultValue: 0,
	        required: false,
			displayDuringSetup: false
		input "MotionSensitivity",
	        "enum",
    	    title: "Motion sensitivity",
			description: "Sensitivity level of the motion sensor",
            options: ["0 - Disabled", "1 - Low", "2 - Medium Low", "3 - Medium", "4 - Medium High", "5 - High"],
			defaultValue: 1,
			required: false,
			displayDuringSetup: true
		input "MotionReset",
	        "number",
    	    title: "Motion reset time",
			description: "Time (in seconds) after a motion event occurs to wait before considering motion to have stopped",
			defaultValue: 20,
			required: false,
			displayDuringSetup: true            
		input "ReportingInterval",
        	"number",
            title: "Reporting interval",
            description: "Time in seconds to check for sensor events",
            defaultValue: 300,
            required: false,
            displayDuringSetup: true
	}
}

private def getDV_CONFIGURED() { "Configured" }
private def getDV_VERSION() { "Application Version" }
private def getNOTIFICATIONTYPE_VIBRATION() { 7 }
private def getSENSORTYPE_TEMPERATURE() { 1 }
private def getSENSORTYPE_ILLUMINATION() { 3 }
private def getSENSORTYPE_HUMIDITY() { 5 }
private def getSENSORTYPE_UV() { 27 }
private def getVIBRATIONEVENT_ACTIVE() { 1 }
private def getVIBRATIONEVENT_INACTIVE() { 0 }
private def getVIBRATIONEVENT_START() { 7 }
private def getVALUE_TRUE() { "true" }
private def getVALUE_FALSE() { "false" }
private def getRV_LUMINANCE() { 128 }
private def getRV_HUMIDITY() { 64 }
private def getRV_TEMPERATURE() { 32 }
private def getRV_UV() { 16 }
private def getRV_BATTERY() { 1 }

private def getmotionSensitivityValueMap()
{
	[
    	"0 - Disabled" : 0, 
        "1 - Low" : 1, 
        "2 - Medium Low" : 2, 
        "3 - Medium" : 3, 
        "4 - Medium High" : 4, 
        "5 - High" : 5
    ]
}
private def log_debug(String message) 
{
	if (state.debug) {
    	log.debug message
    }
}

private sendCommand(physicalgraph.zwave.Command cmd)
{
	log_debug "sendCommand cmd:${cmd}"
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}
private sendCommands(commands, delay=1000) 
{
	log_debug "sendCommands commands:${commands} delay: ${delay}"
	delayBetween(commands.collect{ sendCommand(it) }, delay)
}

def updated()
{
	log_debug "updated()"
	updateDataValue(DV_CONFIGURED, VALUE_FALSE)
	state.debug = (VALUE_TRUE == debugOutput)
	if (state.sec && !isConfigured()) {
		// in case we miss the SCSR
		response(configure())
	}
}

def parse(String description)
{
	log_debug "parse ('${description}')"
	def result = []
    switch(description){
        case ~/Err 106.*/:
			state.sec = 0
			result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
			descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
        break
		case "updated":
        	result = createEvent( name: "Inclusion", value: "paired", isStateChange: true,
			descriptionText: "Update is hit when the device is paired")
            result << response(zwave.wakeUpV1.wakeUpIntervalSet(seconds: 3600, nodeid:zwaveHubNodeId).format())
            result << response(zwave.batteryV1.batteryGet().format())
            result << response(zwave.versionV1.versionGet().format())
            result << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())
            result << response(configure())
        break
        default:
			def cmd = zwave.parse(description, [0x31: 5, 0x30: 2, 0x84: 1])
			if (cmd) {
                try {
				result += zwaveEvent(cmd)
                } catch (e) {
                log.debug "error: $e cmd: $cmd description $description"
                }
			}
        break
	}
    
    log_debug "parse result = '${result}'"
    return result
}

def motionEvent(value)
{
	log_debug "motionEvent (${value})"
	def map = [name: "motion"]
	if (value) {
		map.value = "active"
		map.descriptionText = "$device.displayName detected motion"
	} else {
		map.value = "inactive"
		map.descriptionText = "$device.displayName motion has stopped"
	}
	createEvent(map)
}

def configure() 
{
	log_debug "--Sending configuration command to Multisensor 6--"
    log_debug "Preferences settings: MotionSensitivity: $MotionSensitivity, Reporting Interval: $ReportingInterval, Temp offset: $tempoffset, Humidity offset: $humidityoffset, Luminance offset: $luminanceoffset, UV offset: $ultravioletoffset"
	
	def MotionSens = 1
	if (MotionSensitivity) {
		MotionSens=motionSensitivityValueMap[MotionSensitivity]
	}
    def MotionRst = 20
	if (MotionReset) {
		MotionRst=MotionReset.toInteger()
	}
	def ReportingInt = 300
	if (ReportingInterval) {
		ReportingInt=ReportingInterval.toInteger()
	}
	def tempoff = 0
	if (tempoffset) {
		tempoff=tempoffset*10
	}
	def humidityoff = 0
	if (humidityoffset) {
		humidityoff=humidityoffset
	}
	def luminanceoff = 0
	if (luminanceoffset) {
		luminanceoff=luminanceoffset
	}
	def ultravioletoff = 0
	if (ultravioletoffset) {
		ultravioletoff=ultravioletoffset
	}
    log_debug "settings: ${settings.inspect()}, state: ${state.inspect()}"
	def request = [
		// set wakeup interval to 5 mins
		zwave.wakeUpV1.wakeUpIntervalSet(seconds:300, nodeid:zwaveHubNodeId),
		
		// Get Version information
        zwave.versionV1.versionGet(),
        zwave.firmwareUpdateMdV2.firmwareMdGet(),

		// send temperature, humidity, illuminance, ultraviolet based on reporting interval preference default 5 mins
		zwave.configurationV1.configurationSet(parameterNumber: 0x65, size: 4, scaledConfigurationValue: 128|64|32|16),
		
		// configure frequency of reporting 
		zwave.configurationV1.configurationSet(parameterNumber: 0x6F,size: 4, scaledConfigurationValue: ReportingInt),
		zwave.configurationV1.configurationGet(parameterNumber: 0x6F),
    	
		// send battery every 20 hours
		zwave.configurationV1.configurationSet(parameterNumber: 0x66, size: 4, scaledConfigurationValue: 1),
		zwave.configurationV1.configurationSet(parameterNumber: 0x70, size: 4, scaledConfigurationValue: 20*60*60),
		
        // send no-motion report 20 seconds after motion stops
		zwave.configurationV1.configurationSet(parameterNumber: 0x03, size: 2, scaledConfigurationValue: MotionRst),
		zwave.configurationV1.configurationGet(parameterNumber: 0x03),
    	
		// enable motion sensor and set sensitivity
        zwave.configurationV1.configurationSet(parameterNumber: 0x04, size: 1, scaledConfigurationValue: MotionSens),
		zwave.configurationV1.configurationGet(parameterNumber: 0x04),
    	
		// send binary sensor report for motion
		zwave.configurationV1.configurationSet(parameterNumber: 0x05, size: 1, scaledConfigurationValue: 2),
		
		// Enable the function of touch sensor
        zwave.configurationV1.configurationSet(parameterNumber: 0x07, size: 1, scaledConfigurationValue: 1),
		
		// disable notification-style motion events
		// zwave.notificationV3.notificationSet(notificationType: 7, notificationStatus: 0),
		
        // configure temp offset
		zwave.configurationV1.configurationSet(parameterNumber: 0xC9, size: 1, scaledConfigurationValue: tempoff),
		zwave.configurationV1.configurationGet(parameterNumber: 0xC9),
		
        // configure humidity offset
		zwave.configurationV1.configurationSet(parameterNumber: 0xCA, size: 1, scaledConfigurationValue: humidityoff),
		zwave.configurationV1.configurationGet(parameterNumber: 0xCA),
		
        // configure luminance offset
		zwave.configurationV1.configurationSet(parameterNumber: 0xCB, size: 2, scaledConfigurationValue: luminanceoff),
		zwave.configurationV1.configurationGet(parameterNumber: 0xCB),
		
        // configure ultraviolet offset
		zwave.configurationV1.configurationSet(parameterNumber: 0xCC, size: 1, scaledConfigurationValue: ultravioletoff), 
		zwave.configurationV1.configurationGet(parameterNumber: 0xCC),
        
		zwave.batteryV1.batteryGet(),
		zwave.sensorBinaryV2.sensorBinaryGet(),
        
		// Can use the zwaveHubNodeId variable to add the hub to the device's associations:
		zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)
    ]
	sendCommands(request) + ["delay 20000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
}

private setConfigured()
{
	log_debug "setConfigured ()"
	updateDataValue(DV_CONFIGURED, VALUE_TRUE)
}

private isConfigured()
{
	getDataValue(DV_CONFIGURED) == VALUE_TRUE
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) 
{
	log_debug "zwaveEvent - Wake Up"
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

	if (!isConfigured()) {
		log.warn "Wake up called before configuration is set"
		result += response(configure())
	} else {
		result += response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	result
}
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) 
{	
    log_debug "zwaveEvent - Security Message Encapsulation"

	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x30: 2, 0x7A: 2, 0x84: 1, 0x86: 1])
	state.sec = 1
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) 
{
	log_debug "zwaveEvent - Security Commands Supported Report"
	response(configure())
}
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionCommandClassReport cmd) 
{
	log_debug "zwaveEvent - Version Command Class Report"
	log_debug "${device.displayName} has command class version: ${cmd.commandClassVersion} - payload: ${cmd.payload}"
}
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) 
{
	log_debug "zwaveEvent - Version Report"
	log_debug "${device.displayName} is running firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"

	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue(DV_VERSION, fw)
}
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) 
{
    log_debug "zwaveEvent - Configuration Report v1"
    log_debug "${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd)
{
    log_debug "zwaveEvent - Configuration Report v2"
    log_debug "${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
}
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd)
{
    log_debug "zwaveEvent - Battery Report v1"
	log_debug "${device.displayName} reports battery level of ${cmd.batteryLevel}"
	def result = []
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
		map.descriptionText = "${device.displayName} battery is at ${cmd.batteryLevel}"
		map.isStateChange = true
	}
	state.lastbatt = now()
	result << createEvent(map)
	result
}
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
    log_debug "zwaveEvent - Sensor Multilevel Report v5"
	log_debug "cmd: ${cmd}"

	def map = [:]
	switch (cmd.sensorType) {
		case SENSORTYPE_TEMPERATURE:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			break;
		case SENSORTYPE_ILLUMINATION:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue
			map.unit = "lux"
			break;
        case SENSORTYPE_HUMIDITY:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break;
		case SENSORTYPE_UV:
        	map.name = "ultravioletIndex"
            map.value = cmd.scaledSensorValue.toInteger()
            break;
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd)
{
    log_debug "zwaveEvent - Sensor Binary Report"
	setConfigured()
	motionEvent(cmd.sensorValue)
}
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
    log_debug "zwaveEvent - Basic Set"
	motionEvent(cmd.value)
}
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
    log_debug "zwaveEvent - Notification Report"
	def result = []
	if (cmd.notificationType == NOTIFICATIONTYPE_VIBRATION) {
		switch (cmd.event) {
			case VIBRATIONEVENT_INACTIVE:
				result << motionEvent(0)
				result << createEvent(name: "acceleration", value: "inactive", descriptionText: "$device.displayName tamper cleared")
				break
			case VIBRATIONEVENT_ACTIVE:
				result << createEvent(name: "acceleration", value: "active", descriptionText: "$device.displayName was moved")
				break
			case VIBRATIONEVENT_START:
				result << motionEvent(1)
				break
		}
	} else {
		result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
	result
}
def zwaveEvent(physicalgraph.zwave.Command cmd)
{
    log_debug "zwaveEvent - Generic"
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}