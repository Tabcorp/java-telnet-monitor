#Telnet Monitor

Telnet Monitor Service is an easy way to add a Telnet server to your application. The server is a simple 
query / response service. No login or password is offered so proper network restriction are required.


## Built-in commands

The service comes with a few built in commands :

### exit 
exit the session.

### help
print the available commands. Also accept `?`

## Application properties

The service has 3 configurable properties.

### telnet.service.port
	
Set the port to listen on.

Default is : `9999`

### telnet.service.message.welcome

The welcome message printed when a user connects to the service.

Default is :  `Welcome!`

### telnet.service.message.ready

The prompt given to a user when service is ready to accept a command.

Default is : `Ready for action: `

## Usage

First, create your custom commands by extending the TelnetCommand class and overriding the getShortDescription() and 
execute() methods.

	public class StatusCommand extends TelnetCommand {

		@Override
		public String getShortDescription() {
			return "Show the current status.";
		}

		@Override
		public void execute(PrintWriter printWriter, BufferedReader bufferedReader, String[] strings) throws InvalidArgumentException {
				printWriter.println("Current status is : awesome");
		}
	}


Then register your commands with the service during initialisation: 

	TelnetMonitorService service = new TelnetMonitorService();
	service.registerCommand(new StatusCommand());
