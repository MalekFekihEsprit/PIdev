-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Mar 03, 2026 at 04:09 AM
-- Server version: 10.4.28-MariaDB
-- PHP Version: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `travelmate`
--

-- --------------------------------------------------------

--
-- Table structure for table `activites`
--

CREATE TABLE `activites` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `budget` int(255) NOT NULL,
  `niveaudifficulte` varchar(100) NOT NULL,
  `lieu` varchar(500) DEFAULT NULL,
  `agemin` int(100) NOT NULL,
  `statut` varchar(100) NOT NULL,
  `duree` int(100) NOT NULL,
  `categorie_id` int(11) DEFAULT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `date_prevue` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `activites`
--

INSERT INTO `activites` (`id`, `nom`, `description`, `budget`, `niveaudifficulte`, `lieu`, `agemin`, `statut`, `duree`, `categorie_id`, `image_path`, `date_prevue`) VALUES
(6, 'soirée masquée', '⭐ Une activité exceptionnelle vous attend : soirée masquée à paris ! Niveau facile, cette expérience unique restera gravée dans vos mémoires.', 70, 'Facile', 'paris', 20, 'Active', 4, 3, 'uploads\\activites\\act_20260220_232708.jpg', '2026-02-28'),
(8, 'Randonnée', '🌈 Plongez dans l\'univers magique de Randonnée à Jbal Rsas ! Une activité de niveau difficile qui éveillera tous vos sens.', 450, 'Difficile', 'Dolomites', 15, 'Inactive', 3, 5, 'uploads\\activites\\act_20260220_232623.jpg', NULL),
(15, 'soirée disco', '🔥 L\'aventure vous appelle ! soirée disco à marsa est l\'activité parfaite pour les amateurs de sensations (niveau facile). Venez vivre des moments intenses !', 15, 'Facile', 'marsa', 18, 'Active', 2, 3, 'uploads\\activites\\act_20260224_002508.jpg', NULL),
(17, 'camping', '🎉 Découvrez camping comme vous ne l\'avez jamais vu à Sidi Bou Said, Tunisie ! Activité de niveau difficile, moments de pur bonheur !', 20, 'Difficile', 'Sidi Bou Said, Tunisie', 20, 'En attente', 1, 7, 'uploads\\activites\\act_20260222_231844.jpg', '2026-03-31'),
(20, 'camping', '🌟 Vivez une expérience unique avec camping à jendouba ! Que vous soyez débutant ou expert (niveau expert), cette activité saura vous séduire par son authenticité.', 15, 'Expert', 'jendouba', 15, 'Active', 24, 7, 'uploads\\activites\\act_20260223_235244.jpg', NULL),
(21, 'soirée halloween', '🔥 L\'aventure vous appelle ! soirée halloween à paris est l\'activité parfaite pour les amateurs de sensations (niveau facile). Venez vivre des moments intenses !', 25, 'Facile', 'paris', 18, 'Active', 5, 3, 'uploads\\activites\\act_20260224_002041.jpg', NULL),
(24, 'sortie musée', '🌟 Vivez une expérience unique avec sortie musée à Avenue Abdelaziz Kamel, El Bouhaira, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie ! Cette activité de niveau moyen saura vous séduire.', 15, 'Moyen', 'Avenue Abdelaziz Kamel, El Bouhaira, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie', 15, 'Active', 2, 13, 'uploads\\activites\\act_20260225_234011.jpg', '2026-03-04'),
(26, 'sortie vélo', '🎉 Découvrez sortie vélo comme vous ne l\'avez jamais vu à Rue 8001, Montplaisir, Khereiddine Pacha, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie ! Activité de niveau moyen, moments de pur bonheur !', 15, 'Moyen', 'Rue 8001, Montplaisir, Khereiddine Pacha, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie', 15, 'Active', 2, 5, 'uploads\\activites\\act_20260225_234826.jpg', '2026-03-04'),
(27, 'sortie musée', '🎉 Découvrez sortie musée comme vous ne l\'avez jamais vu à Tour Eiffel, 5, Avenue Anatole France, Quartier du Gros-Caillou, Paris 7e Arrondissement, Paris, Île-de-France, France métropolitaine, 75007, France ! Activité de niveau difficile, moments de pur bonheur !', 20, 'Difficile', 'Tour Eiffel, 5, Avenue Anatole France, Quartier du Gros-Caillou, Paris 7e Arrondissement, Paris, Île-de-France, France métropolitaine, 75007, France', 18, 'Active', 1, 13, 'uploads\\activites\\act_20260226_013624.jpg', '2026-03-05'),
(28, 'sortie musée', '🌟 Vivez une expérience unique avec sortie musée à Allée 1165, Notre Dame, 01 Juin, Délégation El Menzah, Tunis, Gouvernorat Tunis, 1075, Tunisie ! Cette activité de niveau facile saura vous séduire.', 30, 'Facile', 'Allée 1165, Notre Dame, 01 Juin, Délégation El Menzah, Tunis, Gouvernorat Tunis, 1075, Tunisie', 15, 'Inactive', 1, 5, 'uploads\\activites\\act_20260226_023705.jpg', '2026-03-06'),
(29, 'soirée', '✨ Préparez-vous pour une aventure inoubliable ! soirée vous attend à Jardin Japonais, Rue Abou Hamed El Ghazali, Montplaisir, Khereiddine Pacha, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie. Une activité de niveau facile qui vous fera vibrer !', 15, 'Facile', 'Jardin Japonais, Rue Abou Hamed El Ghazali, Montplaisir, Khereiddine Pacha, Délégation Cité El Khadra, Tunis, Gouvernorat Tunis, 1073, Tunisie', 12, 'Active', 2, 3, 'uploads\\activites\\act_20260226_170646.jpg', '2026-03-05'),
(30, 'soirée', '✨ Préparez-vous pour une aventure inoubliable ! soirée vous attend à Institut Supérieur du Sport et de l\'Education Physique de Ksar Saïd, Rue Es-sirt, La Manouba, Ksar - Said, Délégation Manouba‬‬, Gouvernorat La Manouba, 2041, Tunisie. Une activité de niveau moyen qui vous fera vibrer !', 150, 'Moyen', 'Institut Supérieur du Sport et de l\'Education Physique de Ksar Saïd, Rue Es-sirt, La Manouba, Ksar - Said, Délégation Manouba‬‬, Gouvernorat La Manouba, 2041, Tunisie', 15, 'Active', 2, 3, 'uploads\\activites\\act_20260226_173311.jpg', '2026-03-05'),
(31, 'cours de peinture', '🎉 Découvrez cours de peinture comme vous ne l\'avez jamais vu à Boulevard Qualité de la Vie, Gammarth, Délégation La Marsa, Tunis, Gouvernorat Tunis, 1057, Tunisie ! Activité de niveau facile, moments de pur bonheur !', 20, 'Facile', 'Boulevard Qualité de la Vie, Gammarth, Délégation La Marsa, Tunis, Gouvernorat Tunis, 1057, Tunisie', 12, 'Active', 2, 17, 'uploads\\activites\\act_20260226_225545.jpg', '2026-03-05');

-- --------------------------------------------------------

--
-- Table structure for table `budget`
--

CREATE TABLE `budget` (
  `id_budget` int(11) NOT NULL,
  `libelle_budget` varchar(150) NOT NULL,
  `montant_total` double(10,2) NOT NULL,
  `devise_budget` varchar(3) DEFAULT 'EUR',
  `statut_budget` varchar(20) DEFAULT 'ACTIF',
  `description_budget` text DEFAULT NULL,
  `id` int(11) NOT NULL,
  `id_voyage` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `type` varchar(100) NOT NULL,
  `saison` varchar(100) NOT NULL,
  `niveauintensite` varchar(100) NOT NULL,
  `publiccible` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`id`, `nom`, `description`, `type`, `saison`, `niveauintensite`, `publiccible`) VALUES
(3, 'soirée', 'Événements nocturnes pour se divertir, rencontrer du monde et profiter d’ambiances festives.', 'Détente', 'Toutes saisons', 'Moyen', 'adulte'),
(5, 'randonnée', 'Activités en plein air permettant d’explorer la nature à pied, sur des sentiers adaptés à différents niveaux.', 'Aventure', 'Printemps', 'Moyen', 'adulte'),
(7, 'camping', 'Découvrir les activtés \"camping\" que offrent notre application et laissez la nature vous faire rever', 'Aventure', 'Été', 'Élevé', 'adulte'),
(12, 'shopping', 'Activités centrées sur la découverte de boutiques, centres commerciaux et marchés pour faire des achats.', 'Détente', 'Toutes saisons', 'Faible', 'femme'),
(13, 'musée', 'Activités dédiés à la découverte du patrimoine, de l’histoire, de la science ou de l’art à travers des collections et expositions.', 'Culturel', 'Été', 'Faible', 'adulte'),
(17, 'Art', 'Expériences artistiques liées à la créativité comme expositions, ateliers artistiques ou événements culturels.', 'Détente', 'Toutes saisons', 'Faible', 'tout le monde'),
(18, 'sorties conviviales', 'Moments de détente autour de la gastronomie et des boissons dans des lieux conviviaux.', 'Gastronomique', 'Toutes saisons', 'Faible', 'adulte');

-- --------------------------------------------------------

--
-- Table structure for table `delete_notifications`
--

CREATE TABLE `delete_notifications` (
  `id_notification` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `admin_id` int(11) NOT NULL,
  `admin_name` varchar(255) NOT NULL,
  `item_type` varchar(50) NOT NULL,
  `item_id` int(11) NOT NULL,
  `item_name` varchar(255) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `custom_reason` text DEFAULT NULL,
  `deleted_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_read` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `delete_notifications`
--

INSERT INTO `delete_notifications` (`id_notification`, `user_id`, `user_name`, `admin_id`, `admin_name`, `item_type`, `item_id`, `item_name`, `reason`, `custom_reason`, `deleted_at`, `is_read`) VALUES
(1, 2, 'Boutaieb Yosr', 1, 'Neyrouz Chekir', 'Destination', 5, 'Angers (FRANCE)', 'Doublon', NULL, '2026-03-02 16:00:59', 0),
(2, 2, 'Boutaieb Yosr', 1, 'Neyrouz Chekir', 'Hébergement', 15, 'hotel (Appartement)', 'Informations incorrectes', NULL, '2026-03-02 16:04:58', 0),
(3, 3, 'Chekiir Neyrouzz', 1, 'Neyrouz Chekir', 'Destination', 10, 'Hammamet (Tunisia)', 'Informations incorrectes', NULL, '2026-03-02 18:37:34', 1),
(4, 3, 'Chekiir Neyrouzz', 1, 'Neyrouz Chekir', 'Hébergement', 24, 'ihrvz (Villa)', 'Autre', 'jb zvfr', '2026-03-02 18:41:48', 1),
(5, 3, 'Chekiir Neyrouzz', 1, 'Neyrouz Chekir', 'Hébergement', 25, 'Millennium (Hôtel)', 'Autre', 'bcz', '2026-03-02 19:15:41', 1),
(6, 3, 'Boutaieb Yosr', 1, 'Neyrouz Chekir', 'Destination', 11, 'Paris (France)', 'Autre', 'incompatible', '2026-03-03 02:52:24', 1),
(7, 3, 'Boutaieb Yosr', 1, 'Neyrouz Chekir', 'Hébergement', 35, 'hjkrzva hekrv hekfv (Appartement)', 'Informations incorrectes', NULL, '2026-03-03 02:52:35', 1);

-- --------------------------------------------------------

--
-- Table structure for table `depense`
--

CREATE TABLE `depense` (
  `id_depense` int(20) NOT NULL,
  `montant_depense` double(10,2) NOT NULL,
  `libelle_depense` varchar(100) NOT NULL,
  `categorie_depense` varchar(50) NOT NULL,
  `description_depense` text NOT NULL,
  `devise_depense` varchar(3) DEFAULT 'EUR',
  `type_paiement` varchar(30) NOT NULL,
  `date_creation` date NOT NULL,
  `id_budget` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `destination`
--

CREATE TABLE `destination` (
  `id_destination` int(11) NOT NULL,
  `nom_destination` varchar(30) NOT NULL,
  `pays_destination` varchar(30) NOT NULL,
  `region_destination` varchar(100) DEFAULT NULL,
  `description_destination` text DEFAULT NULL,
  `climat_destination` varchar(40) DEFAULT NULL,
  `saison_destination` varchar(40) DEFAULT NULL,
  `latitude_destination` double DEFAULT NULL,
  `longitude_destination` double DEFAULT NULL,
  `score_destination` double DEFAULT NULL,
  `currency_destination` varchar(255) DEFAULT NULL,
  `flag_destination` varchar(500) DEFAULT NULL,
  `languages_destination` varchar(255) DEFAULT NULL,
  `video_url` varchar(255) DEFAULT NULL,
  `added_by` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `destination`
--

INSERT INTO `destination` (`id_destination`, `nom_destination`, `pays_destination`, `region_destination`, `description_destination`, `climat_destination`, `saison_destination`, `latitude_destination`, `longitude_destination`, `score_destination`, `currency_destination`, `flag_destination`, `languages_destination`, `video_url`, `added_by`) VALUES
(1, 'Nice', 'France', NULL, 'Nice, France, is a vibrant Mediterranean jewel...', 'Tropical', 'Été', 43.701944444, 7.268333333, 0, 'EUR (€)', 'https://flagcdn.com/w320/fr.png', 'French', 'https://www.youtube.com/watch?v=jyIux-2o69Y', NULL),
(2, 'Rome', 'Italy', NULL, 'Historic capital with ancient monuments and vibrant culture.', 'Mediterranean', 'Spring', 41.9028, 12.4964, 4.6, 'Euro', 'https://flagcdn.com/w320/it.png', 'Italian', 'https://example.com/rome.mp4', NULL),
(4, 'Tokyo', 'Japan', NULL, 'Modern metropolis blending tradition and technology.', 'Humid Subtropical', 'Autumn', 35.6762, 139.6503, 4.7, 'Yen', 'https://flagcdn.com/w320/jp.png', 'Japanese', 'https://example.com/tokyo.mp4', NULL),
(8, 'New York City', 'USA', 'Americas', 'New York City pulsates with a vibrant energy, a global hub where iconic landmarks like the Statue of Liberty and Times Square meet world-class museums, Broadway shows, and diverse culinary experiences. Immerse yourself in a melting pot of cultures and discover the relentless spirit of the Big Apple.\n\nDay 1: Arrival & Midtown Marvels – Arrive at JFK or Newark, transfer to your hotel in Midtown. Spend the afternoon exploring Times Square, marveling at the lights and energy. Enjoy dinner at a classic New York steakhouse.\n\nDay 2: Statue of Liberty & Financial District – Take a ferry to Liberty Island and Ellis Island, experiencing American history. Afterwards, explore the Financial District, visit Wall Street, and see the Charging Bull statue. Consider a guided walking tour.\n\nDay 3: Museum Mile & Central Park – Dedicate the day to culture! Visit the Metropolitan Museum of Art or the Museum of Modern Art (MoMA). In the afternoon, relax and stroll through Central Park – rent a bike, have a picnic, or visit the Bethesda Terrace.\n\nDay 4: Greenwich Village & Chelsea – Explore the bohemian charm of Greenwich Village, browse independent shops, and enjoy live music.  Head to Chelsea, known for its art galleries and the High Line, an elevated park built on former railway lines.\n\nDay 5: Brooklyn Exploration – Cross the Brooklyn Bridge for stunning city views. Explore DUMBO (Down Under the Manhattan Bridge Overpass) with its cobblestone streets and waterfront park. Enjoy dinner in Brooklyn – pizza is a must!\n\nDay 6: Broadway & Theater District – Immerse yourself in the magic of Broadway!  Attend a show in the Theater District – book tickets in advance.  Explore nearby shops and restaurants before or after the performance.\n\nDay 7:  Shopping & Departure – Depending on your flight time, enjoy some last-minute shopping on Fifth Avenue or in SoHo.  Visit Grand Central Terminal for a final iconic New York moment before heading to the airport for your departure.', 'Tropical', 'Été', 40.71427, -74.00597, 0, 'USD ($)', 'https://flagcdn.com/w320/us.png', 'English', 'https://www.youtube.com/watch?v=nMcvb32H_Hg', 1),
(9, 'Sfax', 'Tunisia', 'Africa', 'Hammamet, Tunisia offers a rich cultural experience, with famous landmarks like the Hammamet Medina and Yasmin Mosque, set amidst a vibrant atmosphere of beautiful beaches and historic architecture.\n\nDay 1: Introduction to Hammamet - Explore the city\'s charming Medina, visit the Great Mosque, and enjoy the local cuisine at a traditional restaurant.\nDay 2: Beach Relaxation - Spend the day relaxing on Hammamet\'s beautiful beaches, such as Hammamet Beach or Yasmin Beach, and take a sunset stroll along the coast.\nDay 3: Historic Excursions - Visit the ancient city of Carthage, a UNESCO World Heritage Site, and explore the Bardo Museum to learn about Tunisia\'s history and culture.\nDay 4: Nature Escapes - Take a day trip to the nearby Cape Bon, known for its stunning natural beauty, and visit the scenic town of Nabeul, famous for its pottery and ceramics.\nDay 5: Water Sports - Enjoy a day of water sports, such as snorkeling, kayaking, or paddleboarding, in the crystal-clear waters of the Mediterranean Sea.\nDay 6: Cultural Immersion - Visit a local market, such as the Souk, to experience the vibrant colors and sounds of Tunisian culture, and attend a traditional folk show in the evening.\nDay 7: Departure - Spend the morning shopping for souvenirs or visiting any last-minute sights, before departing Hammamet and bringing back memories of this enchanting Tunisian city.', 'Méditerranéen', 'Été', 34.74056, 10.76028, 0, 'TND (د.ت)', 'https://flagcdn.com/w320/tn.png', 'Arabic', 'https://www.youtube.com/watch?v=mbEdwymjdq8', 1),
(12, 'Istanbul', 'Turkey', 'Asia', 'Istanbul, a vibrant metropolis straddling Europe and Asia, seamlessly blends ancient history with modern dynamism. Explore magnificent mosques, bustling bazaars, and opulent palaces, immersing yourself in a rich tapestry of Ottoman and Byzantine culture. Experience a captivating atmosphere of contrasts, where East meets West in a truly unforgettable destination.\n\n\n\nDay 1: Arrival & Sultanahmet Exploration – Arrive at Istanbul Airport (IST), transfer to your hotel in the Sultanahmet district. After settling in, begin your exploration with the iconic Hagia Sophia, marveling at its architectural grandeur and historical significance. Finish the day with a delicious Turkish dinner at a local restaurant.\n\nDay 2: Imperial Grandeur – Dedicate the day to the Topkapi Palace, the former residence of Ottoman Sultans. Explore the Harem, Treasury, and Imperial Council chambers. Afterwards, visit the Blue Mosque, famed for its stunning blue Iznik tiles and serene atmosphere.\n\nDay 3: Spice Market & Bazaars – Immerse yourself in the sensory overload of the Spice Market (Egyptian Bazaar), overflowing with fragrant spices, Turkish delight, and local delicacies.  Then, venture into the Grand Bazaar, one of the oldest and largest covered markets in the world, for some serious shopping and people-watching.\n\nDay 4: Cruise the Bosphorus – Embark on a scenic Bosphorus cruise, navigating the waterway that separates Europe and Asia. Admire the waterfront mansions, palaces, and fortresses lining the shores, offering breathtaking views of the city skyline. Enjoy lunch onboard with traditional Turkish cuisine.\n\nDay 5: Süleymaniye Mosque & Local Neighborhoods – Visit the magnificent Süleymaniye Mosque, designed by the renowned architect Sinan. Afterwards, explore the charming neighborhood of Balat, known for its colorful houses and historic synagogues.  Enjoy a traditional Turkish coffee in a local cafe.\n\nDay 6:  Chora Church & Artistic Exploration – Journey to the Chora Church (Kariye Museum), renowned for its exquisite Byzantine mosaics and frescoes.  In the afternoon, explore the trendy Karaköy district, known for its art galleries, cafes, and independent boutiques. Consider a Turkish cooking class for a hands-on cultural experience.\n\nDay 7: Departure – Enjoy a final Turkish breakfast before transferring to Istanbul Airport (IST) for your departure, filled with memories of this captivating city.  Perhaps squeeze in a last-minute souvenir purchase at a local shop.', 'Méditerranéen', 'Été', 41.01, 28.960277777, 3, 'TRY (₺)', 'https://flagcdn.com/w320/tr.png', 'Turkish', 'https://www.youtube.com/watch?v=7A1q7v4btbk', 3);

-- --------------------------------------------------------

--
-- Table structure for table `etape`
--

CREATE TABLE `etape` (
  `id_etape` int(11) NOT NULL,
  `heure` time NOT NULL,
  `description_etape` text NOT NULL,
  `id_activite` int(11) NOT NULL,
  `id_itineraire` int(11) NOT NULL,
  `numero_jour` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `hebergement`
--

CREATE TABLE `hebergement` (
  `id_hebergement` int(11) NOT NULL,
  `nom_hebergement` varchar(100) NOT NULL,
  `type_hebergement` varchar(50) DEFAULT NULL,
  `prixNuit_hebergement` double DEFAULT NULL,
  `adresse_hebergement` varchar(100) DEFAULT NULL,
  `note_hebergement` double DEFAULT NULL,
  `latitude_hebergement` double DEFAULT NULL,
  `longitude_hebergement` double DEFAULT NULL,
  `destination_hebergement` int(11) NOT NULL,
  `added_by` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `hebergement`
--

INSERT INTO `hebergement` (`id_hebergement`, `nom_hebergement`, `type_hebergement`, `prixNuit_hebergement`, `adresse_hebergement`, `note_hebergement`, `latitude_hebergement`, `longitude_hebergement`, `destination_hebergement`, `added_by`) VALUES
(33, 'Eventi', 'Hôtel', 0, 'Eventi, 851 6th Avenue, New York, NY 10001, United States of America', 0, 40.74716785036294, -73.99009722639826, 8, 1),
(34, 'The Bowery Hotel', 'Hôtel', 0, 'The Bowery Hotel, 335 Bowery, New York, NY 10003, United States of America', 0, 40.726037350360414, -73.9914423397135, 8, 1),
(36, 'Grand Hyatt Istanbul', 'Hôtel', 0, 'Grand Hyatt Istanbul, Asker Ocağı Caddesi 1, 34367 Şişli, Turkey', 5, 41.04100715039671, 28.988474371069866, 12, 3),
(37, 'Grand Hotel de Londres', 'Hôtel', 0, 'Grand Hotel de Londres, Meşrutiyet Caddesi 53, 34430 Beyoğlu, Turkey', 4, 41.03239540039574, 28.975235000000005, 12, 3);

-- --------------------------------------------------------

--
-- Table structure for table `itineraire`
--

CREATE TABLE `itineraire` (
  `id_itineraire` int(11) NOT NULL,
  `nom_itineraire` varchar(100) NOT NULL,
  `description_itineraire` text NOT NULL,
  `id_voyage` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `liste_activite`
--

CREATE TABLE `liste_activite` (
  `id_voyage` int(11) NOT NULL,
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `paiement`
--

CREATE TABLE `paiement` (
  `id_paiement` int(11) NOT NULL,
  `id_voyage` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `montant` decimal(10,2) NOT NULL,
  `devise` varchar(10) NOT NULL DEFAULT 'EUR',
  `methode` varchar(50) NOT NULL,
  `statut` varchar(50) NOT NULL DEFAULT 'EN_ATTENTE',
  `transaction_id` varchar(255) DEFAULT NULL,
  `sale_id` varchar(255) DEFAULT NULL,
  `date_paiement` timestamp NOT NULL DEFAULT current_timestamp(),
  `description` text DEFAULT NULL,
  `email_payeur` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `paiement`
--

INSERT INTO `paiement` (`id_paiement`, `id_voyage`, `id_utilisateur`, `montant`, `devise`, `methode`, `statut`, `transaction_id`, `sale_id`, `date_paiement`, `description`, `email_payeur`) VALUES
(1, 23, 1, 150.00, 'EUR', 'STRIPE', 'COMPLETE', 'pi_3T46etBBrkfMn6Hw0qXRSJ06', NULL, '2026-02-23 20:27:13', 'Paiement pour le voyage : amal (Modifié)', 'client@example.com'),
(2, 23, 1, 150.00, 'EUR', 'STRIPE', 'COMPLETE', 'pi_3T46fvBBrkfMn6Hw1EOb0huN', NULL, '2026-02-23 20:28:18', 'Paiement pour le voyage : amal (Modifié)', 'minyar.ghannem@esprit.tn'),
(3, 23, 1, 150.00, 'EUR', 'STRIPE', 'COMPLETE', 'pi_3T46g8BBrkfMn6Hw1vta3DUU', NULL, '2026-02-23 20:28:30', 'Paiement pour le voyage : amal (Modifié)', 'minyar.ghannem@esprit.tn'),
(4, 23, 1, 150.00, 'EUR', 'STRIPE', 'COMPLETE', 'pi_3T474EBBrkfMn6Hw1nmNpWVP', NULL, '2026-02-23 20:53:24', 'Paiement pour le voyage : amal (Modifié)', 'mimi@gmail.com'),
(5, 23, 1, 150.00, 'EUR', 'STRIPE', 'COMPLETE', 'pi_3T47khBBrkfMn6Hw1ka9p8C5', NULL, '2026-02-23 21:37:18', 'Paiement pour le voyage : amal (Modifié)', 'mimi@gmail.com'),
(6, 23, 1, 150.00, 'EUR', 'STRIPE', 'COMPLETE', 'pi_3T4JRLBBrkfMn6Hw0Su8emQh', NULL, '2026-02-24 10:06:05', 'Paiement pour le voyage : amal (Modifié)', 'client@example.com'),
(7, 23, 1, 150.00, 'EUR', 'STRIPE', 'COMPLETE', 'pi_3T4V0xBBrkfMn6Hw0saTG70H', NULL, '2026-02-24 22:27:38', 'Paiement pour le voyage : amal (Modifié)', 'mimi@gmail.com'),
(8, 23, 1, 150.00, 'EUR', 'STRIPE', 'COMPLETE', 'pi_3T4V1LBBrkfMn6Hw0GDdhdZR', NULL, '2026-02-24 22:28:03', 'Paiement pour le voyage : amal (Modifié)', 'ilyessguesmi7@gmail.com'),
(9, 23, 1, 150.00, 'EUR', 'STRIPE', 'COMPLETE', 'pi_3T4V9vBBrkfMn6Hw0sztRk2v', NULL, '2026-02-24 22:36:55', 'Paiement pour le voyage : amal (Modifié)', 'ilyessguesmi7@gmail.com');

-- --------------------------------------------------------

--
-- Table structure for table `participation`
--

CREATE TABLE `participation` (
  `id_participation` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `role_participation` varchar(50) NOT NULL,
  `id_voyage` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `participation`
--

INSERT INTO `participation` (`id_participation`, `id`, `role_participation`, `id_voyage`) VALUES
(1, 5, 'Participant', 29);

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `date_naissance` date NOT NULL,
  `email` varchar(150) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER',
  `photo_url` varchar(500) DEFAULT NULL,
  `verification_code` varchar(6) DEFAULT NULL,
  `is_verified` tinyint(1) DEFAULT 0,
  `last_login_ip` varchar(45) DEFAULT NULL,
  `last_login_location` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `photo_file_name` varchar(255) DEFAULT NULL,
  `face_embedding` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `nom`, `prenom`, `date_naissance`, `email`, `telephone`, `mot_de_passe`, `role`, `photo_url`, `verification_code`, `is_verified`, `last_login_ip`, `last_login_location`, `created_at`, `photo_file_name`, `face_embedding`) VALUES
(1, 'Chekir', 'Neyrouz', '2005-01-29', 'neyrouzchekir01@gmail.com', '+21629074810', '$2a$12$HZAHY7Nb1zbudlwU28ienuKGjzKDUlPPVw9wBOdEcCFZVbLLhN6Je', 'ADMIN', NULL, NULL, 1, '197.16.51.235', 'Tunis, Tunisia, ', '2026-02-26 18:12:29', NULL, NULL),
(2, 'Boutaieb', 'Yosr', '2003-05-30', 'yosr.boutaieb@esprit.tn', NULL, '$2a$12$K52nqVrAtvx8EMHTyi7kFO1VFax0SQ5nlsExmunmWbG4h3JsKJ4YO', 'USER', NULL, NULL, 0, NULL, NULL, '2026-02-26 18:16:36', NULL, NULL),
(3, 'Boutaieb', 'Yosr', '2003-02-08', 'neyrouz.chekir@esprit.tn', NULL, '$2a$12$xru.KeGJexFAPNBrxxdWk.20aMGwuZ/fZmdMfy/YttCSSCuVHtMva', 'USER', NULL, NULL, 1, '197.16.51.235', 'Tunis, Tunisia, ', '2026-02-26 18:40:58', NULL, NULL),
(4, 'Boutaieb', 'Yosr', '2004-02-13', 'neyrouzchekir2005@gmail.com', NULL, '$2a$12$/KPn2qOG5U5wOBL7Wvr/XuPag3EvRbAJPivloQ1OKj8yQzi3B9Y/O', 'USER', NULL, NULL, 1, '41.230.136.167', 'Tunis, Tunisia, ', '2026-02-26 19:00:33', NULL, NULL),
(5, 'minyar', 'ghannem', '2026-02-14', 'minyarghannem06@gmail.com', '92559234', 'Travelmate123*', 'USER', '', NULL, 0, NULL, NULL, '2026-02-28 13:16:39', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `voyage`
--

CREATE TABLE `voyage` (
  `id_voyage` int(11) NOT NULL,
  `titre_voyage` varchar(100) NOT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `statut` varchar(50) NOT NULL,
  `id_destination` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `voyage`
--

INSERT INTO `voyage` (`id_voyage`, `titre_voyage`, `date_debut`, `date_fin`, `statut`, `id_destination`) VALUES
(3, 'minyar', '2026-02-12 00:00:00', '2026-02-27 00:00:00', 'a venir', 1),
(4, 'meriemmmk', '2026-02-13 00:00:00', '2026-02-27 00:00:00', 'a venir', 1),
(7, 'DOUDOUZo', '2026-02-27 00:00:00', '2026-03-08 00:00:00', 'Terminé', 1),
(8, 'laraGH', '2026-02-20 00:00:00', '2026-03-27 00:00:00', 'Annulé', 1),
(9, 'amira', '2026-02-14 00:00:00', '2026-03-06 00:00:00', 'a venir', 1),
(10, 'yosr', '2026-02-12 00:00:00', '2026-03-08 00:00:00', 'En cours', 1),
(13, 'ADAM', '2026-02-15 00:00:00', '2026-02-17 00:00:00', 'Terminé', 1),
(14, 'ZAKIAAAA', '2026-02-15 00:00:00', '2026-02-21 00:00:00', 'a venir', 1),
(15, 'Nouuur', '2026-02-15 00:00:00', '2026-02-20 00:00:00', 'a venir', 1),
(23, 'amal (Modifié)', '2026-07-16 00:00:00', '2026-08-01 00:00:00', 'a venir', 1),
(28, 'ILYES', '2026-02-25 00:00:00', '2026-02-27 00:00:00', 'a venir', 2),
(29, 'voyageillll', '2026-02-25 00:00:00', '2026-02-26 00:00:00', 'a venir', 1),
(31, 'www', '2026-02-27 00:00:00', '2026-03-04 00:00:00', 'a venir', 2),
(51, 'backtest', '2026-02-28 00:00:00', '2026-03-01 00:00:00', 'a venir', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activites`
--
ALTER TABLE `activites`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_activites_categories` (`categorie_id`);

--
-- Indexes for table `budget`
--
ALTER TABLE `budget`
  ADD PRIMARY KEY (`id_budget`),
  ADD KEY `fk_budget_user` (`id`),
  ADD KEY `fk_budget_voyage` (`id_voyage`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `delete_notifications`
--
ALTER TABLE `delete_notifications`
  ADD PRIMARY KEY (`id_notification`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `admin_id` (`admin_id`);

--
-- Indexes for table `depense`
--
ALTER TABLE `depense`
  ADD PRIMARY KEY (`id_depense`),
  ADD KEY `depense_fk` (`id_budget`);

--
-- Indexes for table `destination`
--
ALTER TABLE `destination`
  ADD PRIMARY KEY (`id_destination`),
  ADD KEY `fk_destination_user` (`added_by`);

--
-- Indexes for table `etape`
--
ALTER TABLE `etape`
  ADD PRIMARY KEY (`id_etape`),
  ADD KEY `fk_etape_itineraire` (`id_itineraire`),
  ADD KEY `fk_etape_activite` (`id_activite`);

--
-- Indexes for table `hebergement`
--
ALTER TABLE `hebergement`
  ADD PRIMARY KEY (`id_hebergement`),
  ADD KEY `hebergement_fk` (`destination_hebergement`),
  ADD KEY `fk_hebergement_user` (`added_by`);

--
-- Indexes for table `itineraire`
--
ALTER TABLE `itineraire`
  ADD PRIMARY KEY (`id_itineraire`),
  ADD KEY `fk_itineraire_voyage` (`id_voyage`);

--
-- Indexes for table `liste_activite`
--
ALTER TABLE `liste_activite`
  ADD KEY `fk_liste_activite_voyage` (`id_voyage`),
  ADD KEY `fk_liste_activite_activite` (`id`);

--
-- Indexes for table `paiement`
--
ALTER TABLE `paiement`
  ADD PRIMARY KEY (`id_paiement`),
  ADD KEY `id_voyage` (`id_voyage`),
  ADD KEY `id_utilisateur` (`id_utilisateur`);

--
-- Indexes for table `participation`
--
ALTER TABLE `participation`
  ADD PRIMARY KEY (`id_participation`),
  ADD KEY `fk_participation_user` (`id`),
  ADD KEY `fk_participation_voyage` (`id_voyage`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `voyage`
--
ALTER TABLE `voyage`
  ADD PRIMARY KEY (`id_voyage`),
  ADD KEY `fk_voyage_destination` (`id_destination`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `activites`
--
ALTER TABLE `activites`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT for table `budget`
--
ALTER TABLE `budget`
  MODIFY `id_budget` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `delete_notifications`
--
ALTER TABLE `delete_notifications`
  MODIFY `id_notification` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `depense`
--
ALTER TABLE `depense`
  MODIFY `id_depense` int(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `destination`
--
ALTER TABLE `destination`
  MODIFY `id_destination` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `etape`
--
ALTER TABLE `etape`
  MODIFY `id_etape` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `hebergement`
--
ALTER TABLE `hebergement`
  MODIFY `id_hebergement` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=38;

--
-- AUTO_INCREMENT for table `itineraire`
--
ALTER TABLE `itineraire`
  MODIFY `id_itineraire` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `paiement`
--
ALTER TABLE `paiement`
  MODIFY `id_paiement` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `participation`
--
ALTER TABLE `participation`
  MODIFY `id_participation` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `voyage`
--
ALTER TABLE `voyage`
  MODIFY `id_voyage` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=52;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activites`
--
ALTER TABLE `activites`
  ADD CONSTRAINT `fk_activites_categories` FOREIGN KEY (`categorie_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `budget`
--
ALTER TABLE `budget`
  ADD CONSTRAINT `fk_budget_user` FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_budget_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `delete_notifications`
--
ALTER TABLE `delete_notifications`
  ADD CONSTRAINT `fk_notification_admin` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `depense`
--
ALTER TABLE `depense`
  ADD CONSTRAINT `depense_fk` FOREIGN KEY (`id_budget`) REFERENCES `budget` (`id_budget`) ON DELETE CASCADE;

--
-- Constraints for table `destination`
--
ALTER TABLE `destination`
  ADD CONSTRAINT `fk_destination_user` FOREIGN KEY (`added_by`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `etape`
--
ALTER TABLE `etape`
  ADD CONSTRAINT `fk_etape_activite` FOREIGN KEY (`id_activite`) REFERENCES `activites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_etape_itineraire` FOREIGN KEY (`id_itineraire`) REFERENCES `itineraire` (`id_itineraire`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `hebergement`
--
ALTER TABLE `hebergement`
  ADD CONSTRAINT `fk_hebergement_user` FOREIGN KEY (`added_by`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `hebergement_fk` FOREIGN KEY (`destination_hebergement`) REFERENCES `destination` (`id_destination`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `itineraire`
--
ALTER TABLE `itineraire`
  ADD CONSTRAINT `fk_itineraire_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `liste_activite`
--
ALTER TABLE `liste_activite`
  ADD CONSTRAINT `fk_liste_activite_activite` FOREIGN KEY (`id`) REFERENCES `activites` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_liste_activite_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `paiement`
--
ALTER TABLE `paiement`
  ADD CONSTRAINT `paiement_ibfk_1` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE,
  ADD CONSTRAINT `paiement_ibfk_2` FOREIGN KEY (`id_utilisateur`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `participation`
--
ALTER TABLE `participation`
  ADD CONSTRAINT `fk_participation_user` FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_participation_voyage` FOREIGN KEY (`id_voyage`) REFERENCES `voyage` (`id_voyage`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `voyage`
--
ALTER TABLE `voyage`
  ADD CONSTRAINT `fk_voyage_destination` FOREIGN KEY (`id_destination`) REFERENCES `destination` (`id_destination`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
