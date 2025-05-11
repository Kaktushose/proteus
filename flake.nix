{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
  };

  outputs = {
    self,
    flake-parts,
    ...
  } @ inputs:
    flake-parts.lib.mkFlake {inherit inputs;} {
      systems = ["x86_64-linux"];

      perSystem = {
        config,
        lib,
        pkgs,
        system,
        ...
      }: let
        jdk-linux-base = pkgs.fetchurl {
            url = "https://raw.githubusercontent.com/NixOS/nixpkgs/c04d7170e047829f03c514abb5a1aa98f58f7b80/pkgs/development/compilers/temurin-bin/jdk-linux-base.nix";
            hash = "sha256-uAp2+F6fAlVXKLdwsl8aYOt3fUi/mYj40wYZSUeNNQw=";
        };

        common = opts: pkgs.callPackage (import jdk-linux-base opts) { };

        jdk24 = common { sourcePerArch = {
            packageType = "jdk";
            vmType = "hotspot";
            x86_64 = {
                build = "9";
                sha256 = "78832cb5ea4074f2215cde0d01d6192d09c87636fc24b36647aea61fb23b8272";
                url = "https://github.com/adoptium/temurin24-binaries/releases/download/jdk-24.0.1%2B9/OpenJDK24U-jdk_x64_linux_hotspot_24.0.1_9.tar.gz";
                version = "24.0.1";
            };
        };};

        jdk = pkgs.temurin-bin-23;

        gradle = pkgs.gradle.override {
            javaToolchains = [
                jdk
            ];
            java = jdk;
        };
       in {
         devShells.default = pkgs.mkShell {
           name = "Jack";
           packages = with pkgs; [git jdk gradle maven];
           JDK24 = jdk;
         };
       };
    };
}