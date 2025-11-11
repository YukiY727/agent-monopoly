{
  description = "Monopoly game simulation project";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            # Java 21
            jdk21

            # Build tool
            gradle

            # Git
            git

            # Optional: useful tools
            tree
            ripgrep
            fzf
          ];

          shellHook = ''
            echo "Monopoly Development Environment"
            echo "================================"
            echo "Java version:"
            java -version
            echo ""
            echo "Gradle version:"
            gradle -version
            echo ""
            echo "Ready to start Phase 1 development!"
          '';

          # Environment variables
          JAVA_HOME = "${pkgs.jdk21}";
          GRADLE_USER_HOME = "./.gradle";
        };
      }
    );
}
