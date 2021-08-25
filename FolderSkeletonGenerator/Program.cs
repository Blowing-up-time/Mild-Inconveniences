using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace FolderSkeletonGenerator
{
	class Program
	{
		public static string ExeName
		{
			get
			{
				return AppDomain.CurrentDomain.FriendlyName;
			}
		}
		private static string FileName { get; set; }

		//Might add a logging function later.
		private static string[] Log { get; set; }
		static void Main(string[] args)
		{
			argsParser(args);
		}
		public static void argsParser(string[] args)
		{
			if (args.Length > 0)
			{
				var i = 0;
				if (args[i] == "--help" || args[i] == "-h")
				{
					Console.WriteLine("Welcome to the Folder Skeleton Generator!\n" +
						"Usage: " + System.AppDomain.CurrentDomain.FriendlyName + ".exe  [--help]\n" +
						"\t\t[--file <filename>]\n\n" +
						"List of Arguments:\n" +
						"\t--file <FileName>:\tSpecify the file to use as the folder skeleton\n" +
						"\t--help:\t\t\tDisplay this menu\n\n" +
						"Shorthand Arguments: \n" +
						"\t-f:\t\tShorthand for --file\n" +
						"\t-h:\t\tShorthand for --help\n\n" +
						"That's all for now!");
				}
				else if (args[i] == "--file" || args[i] == "-f")
				{
					if (i + 1 >= args.Length)
					{
						Console.WriteLine("Please specify a file");
					}
					else
					{
                        try
                        {
							FileName = args[i + 1];

							string[] lines = File.ReadAllLines(@".\" + FileName);
							Console.WriteLine("Using file {0} as tree Skeleton Template", FileName);
							string[] dirs = convertToDirectory(lines);
							Console.WriteLine("Creating Directories...");
							createDirectories(dirs);
							Console.WriteLine("Directory Structure created under folder \"Skeleton\"");
						}
                        catch (FileNotFoundException e)
                        {
							Console.WriteLine("File not found. Please check the filename and try again\n" +
								"Error Info: {0}", e.Source);
                            throw;
                        }
						catch (UnauthorizedAccessException e)
                        {
							Console.WriteLine("You do not seem to have access to this file or directory\n" +
								"Error Info: {0}", e.Source);
                        }
					}

				}
				else
				{
					Console.WriteLine("Invalid or no arguments. Please use \"" + ExeName + " --help\" to see a list of available commands");
				}
			}
			else
			{
				Console.WriteLine("Invalid or no arguments. Please use \"" + ExeName + " --help\" to see a list of available commands");
			}
		}

		public static string[] convertToDirectory(string[] lines)
		{
			List<string> directories = new List<string>();
			for (int i = 0; i < lines.Length; i++)
			{
				int indexCount = lines[i].Count(s => s.ToString() == "\t");
				string[] parts = lines[i].Split("\t");
				if (indexCount > 0)
				{
					bool done = false;
					int j = i;
					
					int indexOfString = countTabs(lines[i]);

					int lastCheckedTabs = countTabs(lines[i]);
					while (!done)
					{
						if (parts.Length == 1)
                        {
							
							done = true;
                        }
						else if (countTabs(lines[j]) < lastCheckedTabs)
						{
							//do working here
							string dirToAdd = lines[j];
							dirToAdd = dirToAdd.Replace("\t", String.Empty);
							parts[indexOfString] = dirToAdd + @"\" + parts[indexOfString];
							List<string> fds = parts.ToList();
							fds.RemoveAt(indexOfString - 1);
							parts = fds.ToArray();
							lastCheckedTabs = countTabs(lines[j]);
							indexOfString--;
							j--;
						}
						else
						{
							j--;
						}
					}
				}
				directories.Add(parts[0]);
			}
			return directories.ToArray();
		}

		public static int countTabs(string s)
		{
			return s.Count(s => s.ToString() == "\t");
		}
		public static void createDirectories(string[] directoriesToCreate)
        {
            foreach (var dir in directoriesToCreate)
            {
				Directory.CreateDirectory("Skeleton\\" + dir);
            }
        }
	}
}