import * as fs from 'fs';
import * as path from 'path';
import * as vscode from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions, TransportKind } from 'vscode-languageclient/node';

let client: LanguageClient | undefined;

export async function activate(context: vscode.ExtensionContext): Promise<void> {
  const command = serverCommand(context);
  if (!fs.existsSync(command)) {
    vscode.window.showErrorMessage(
      `MiniC++ language server not found at ${command}. Run './gradlew :lsp:installDist' or set 'minicpp.server.path'.`);
    return;
  }

  // the .bat start script on Windows must run through a shell, which needs
  // the path quoted in case it contains spaces
  const windows = process.platform === 'win32';
  const serverOptions: ServerOptions = {
    command: windows ? `"${command}"` : command,
    transport: TransportKind.stdio,
    options: { shell: windows },
  };
  const clientOptions: LanguageClientOptions = {
    documentSelector: [{ scheme: 'file', language: 'minicpp' }],
  };

  client = new LanguageClient('minicpp', 'MiniC++ Language Server', serverOptions, clientOptions);
  await client.start();
}

export async function deactivate(): Promise<void> {
  await client?.stop();
}

/** The configured start script, or the one built in this repository. */
function serverCommand(context: vscode.ExtensionContext): string {
  const configured = vscode.workspace.getConfiguration('minicpp').get<string>('server.path');
  if (configured) {
    return configured;
  }
  const script = process.platform === 'win32' ? 'minicpp-lsp.bat' : 'minicpp-lsp';
  return path.join(context.extensionPath, '..', 'lsp', 'build', 'install', 'minicpp-lsp', 'bin', script);
}
