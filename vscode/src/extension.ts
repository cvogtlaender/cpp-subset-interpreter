import * as fs from 'fs';
import * as path from 'path';
import * as vscode from 'vscode';
import { Executable, LanguageClient, LanguageClientOptions, ServerOptions, TransportKind } from 'vscode-languageclient/node';

const MAIN_CLASS = 'de.cvogtlaender.lsp.MiniCppLanguageServerMain';

let client: LanguageClient | undefined;

export async function activate(context: vscode.ExtensionContext): Promise<void> {
  const server = serverExecutable(context);
  if (!server) {
    vscode.window.showErrorMessage(
      "MiniC++ language server not found. Run './gradlew :lsp:installDist' or set 'minicpp.server.path'.");
    return;
  }

  const serverOptions: ServerOptions = server;
  const clientOptions: LanguageClientOptions = {
    documentSelector: [{ scheme: 'file', language: 'minicpp' }],
  };

  client = new LanguageClient('minicpp', 'MiniC++ Language Server', serverOptions, clientOptions);
  try {
    await client.start();
  } catch (e) {
    vscode.window.showErrorMessage(
      `MiniC++ language server failed to start (Java 21 or newer is required, see 'minicpp.java.path'): ${e}`);
  }
}

export async function deactivate(): Promise<void> {
  await client?.stop();
}

/**
 * How to start the server, in order of preference: the configured start
 * script, the server bundled into the extension package, or the one built in
 * this repository.
 */
function serverExecutable(context: vscode.ExtensionContext): Executable | undefined {
  const config = vscode.workspace.getConfiguration('minicpp');
  const windows = process.platform === 'win32';

  const configured = config.get<string>('server.path');
  if (configured) {
    return fs.existsSync(configured) ? script(configured, windows) : undefined;
  }

  // started with java directly: start scripts lose their executable bit in a .vsix
  const bundled = path.join(context.extensionPath, 'server', 'lib');
  if (fs.existsSync(bundled)) {
    return {
      command: javaCommand(config, windows),
      args: ['-cp', path.join(bundled, '*'), MAIN_CLASS],
      transport: TransportKind.stdio,
    };
  }

  const name = windows ? 'minicpp-lsp.bat' : 'minicpp-lsp';
  const repository = path.join(context.extensionPath, '..', 'lsp', 'build', 'install', 'minicpp-lsp', 'bin', name);
  return fs.existsSync(repository) ? script(repository, windows) : undefined;
}

// the .bat start script on Windows must run through a shell, which needs the
// path quoted in case it contains spaces
function script(command: string, windows: boolean): Executable {
  return {
    command: windows ? `"${command}"` : command,
    transport: TransportKind.stdio,
    options: { shell: windows },
  };
}

function javaCommand(config: vscode.WorkspaceConfiguration, windows: boolean): string {
  const configured = config.get<string>('java.path');
  if (configured) {
    return configured;
  }
  const exe = windows ? 'java.exe' : 'java';
  const home = process.env.JAVA_HOME;
  if (home && fs.existsSync(path.join(home, 'bin', exe))) {
    return path.join(home, 'bin', exe);
  }
  return 'java';
}
